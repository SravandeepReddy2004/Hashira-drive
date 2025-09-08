import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class Main{

    static class Share {
        final BigInteger x;
        final BigInteger y;
        public Share(BigInteger x, BigInteger y) { this.x = x; this.y = y; }
        @Override
        public String toString() { return "Share(x=" + x + ", y=" + y + ")"; }
    }

    private static List<Share> parseShares(String jsonInput, int[] kOut) throws JSONException, NumberFormatException {
        List<Share> shares = new ArrayList<>();
        JSONObject rootObject = new JSONObject(jsonInput);
        JSONObject keysObject = rootObject.getJSONObject("keys");
        kOut[0] = keysObject.getInt("k");
        Iterator<String> keyIterator = rootObject.keys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            if (key.equals("keys")) continue;
            JSONObject shareObject = rootObject.getJSONObject(key);
            int base = Integer.parseInt(shareObject.getString("base"));
            String valueStr = shareObject.getString("value");
            BigInteger x = new BigInteger(key);
            BigInteger y = new BigInteger(valueStr, base);
            shares.add(new Share(x, y));
        }
        return shares;
    }

    public static BigInteger findSecret(List<Share> shares, int k) {
    List<Share> points = shares.subList(0, k);
    BigInteger secret = BigInteger.ZERO;

    for (int j = 0; j < k; j++) {
        Share currentPoint = points.get(j);
        BigInteger numerator = BigInteger.ONE;
        BigInteger denominator = BigInteger.ONE;

        for (int m = 0; m < k; m++) {
            if (j == m) continue;
            BigInteger x_m = points.get(m).x;
            numerator = numerator.multiply(x_m);
            denominator = denominator.multiply(x_m.subtract(currentPoint.x));
        }

        BigInteger term = currentPoint.y.multiply(numerator).divide(denominator);
        secret = secret.add(term);
    }
    return secret.abs();
}


    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java HashiraShamirSolver <path_to_json_file>");
            return;
        }
        String filePath = args[0];
        System.out.println("--- Reading Test Case from: " + filePath + " ---");
        try {
            String jsonInput = new String(Files.readAllBytes(Paths.get(filePath)));
            int[] k = new int[1];
            List<Share> shares = parseShares(jsonInput, k);
            if (shares.size() < k[0]) {
                System.err.println("Error: Not enough shares. Required: " + k[0] + ", Found: " + shares.size());
                return;
            }
            BigInteger secret = findSecret(shares, k[0]);
            System.out.println("✅ The reconstructed secret is: " + secret);
        } catch (IOException e) {
            System.err.println("Error: Could not read the file '" + filePath + "'.");
        } catch (JSONException e) {
            System.err.println("Error: Invalid JSON format. " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format in JSON. " + e.getMessage());
        } catch (ArithmeticException e) {
            System.err.println("Error during calculation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

public class Main {

    static class Share {
        final BigInteger x;
        final BigInteger y;
        public Share(BigInteger x, BigInteger y) { this.x = x; this.y = y; }
        @Override
        public String toString() { return "Share(x=" + x + ", y=" + y + ")"; }
    }

    private static List<Share> parseShares(String jsonInput, int[] kOut) {
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

    // Modular Lagrange interpolation
    public static BigInteger findSecret(List<Share> shares, int k, BigInteger p) {
        List<Share> points = shares.subList(0, k);
        BigInteger secret = BigInteger.ZERO;

        for (int j = 0; j < k; j++) {
            Share current = points.get(j);
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int m = 0; m < k; m++) {
                if (j == m) continue;
                BigInteger x_m = points.get(m).x;
                numerator = numerator.multiply(x_m).mod(p);
                denominator = denominator.multiply(x_m.subtract(current.x)).mod(p);
            }

            BigInteger invDenom = denominator.modInverse(p);
            BigInteger term = current.y.multiply(numerator).mod(p).multiply(invDenom).mod(p);
            secret = secret.add(term).mod(p);
        }

        return secret; // Always 0 <= secret < p
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -cp .;json-20210307.jar Main <path_to_json_file>");
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

            // Pick a prime larger than all y-values
            BigInteger maxY = shares.stream().map(s -> s.y).max(BigInteger::compareTo).get();
            BigInteger p = maxY.nextProbablePrime();

            BigInteger secret = findSecret(shares, k[0], p);
            System.out.println("✅ The reconstructed secret (mod " + p + ") is: " + secret);

        } catch (IOException e) {
            System.err.println("Error: Could not read the file '" + filePath + "'.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}

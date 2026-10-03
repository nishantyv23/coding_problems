import java.io.*;
import java.util.*;

public class FraudDetection
{

    static class Charge
    {
        String chargeId;
        String accountId;
        String code;

        boolean fraudulent;
        boolean disputed;

        Charge(String chargeId, String accountId, String code, boolean fraudulent, boolean disputed) {
            this.chargeId = chargeId;
            this.accountId = accountId;
            this.code = code;
            this.fraudulent = fraudulent;
            this.disputed = disputed;
        }
    }

    static class Merchant
    {

        String accountId;
        String mcc;

        // Maximum allowed fraction of fraudulent transactions.
        double fraudThreshold;

        int totalTransactions;
        int fraudulentTransactions;

        /*
         * Current fraud status.
         *
         * Unlike Part 2, this can become false again after
         * a dispute overturns the fraudulent transaction(s).
         */
        boolean fraudulent;

        Merchant(String accountId, String mcc, double fraudThreshold)
        {
            this.accountId = accountId;
            this.mcc = mcc;
            this.fraudThreshold = fraudThreshold;
        }

        //Add a new transaction
        void addTransaction(boolean isFraudulent, boolean isDisputed, int minimumTransactions) {
            totalTransactions++;

            if (isFraudulent && !isDisputed) {
                fraudulentTransactions++;
            }

            evaluate(minimumTransactions);
        }

        /*
         * A dispute changes an existing transaction from
         * fraudulent -> non-fraudulent.
         */
        void disputeFraudulentTransaction(int minimumTransactions) {
            if (fraudulentTransactions > 0) {
                fraudulentTransactions--;
            }

            evaluate(minimumTransactions);
        }

        // Re-evaluate the current fraud status.
        void evaluate(int minimumTransactions)
        {
            /*
             * We cannot evaluate before the minimum number
             * of transactions has been observed.
             */
            if (totalTransactions < minimumTransactions) {
                fraudulent = false;
                return;
            }

            // Avoid integer division
            double fraudPercentage = (double) fraudulentTransactions/totalTransactions;

            /*
             * Part 2 rule:
             * fraud percentage >= threshold
             */
            fraudulent = fraudPercentage >= fraudThreshold;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Set<String> fraudCodes = parseCodes(br.readLine());

        /*
         * ---------------------------------------------------------
         * 3. MCC thresholds
         * MCC,fraction
         * ---------------------------------------------------------
         */
        int mccCount = Integer.parseInt(br.readLine().trim());

        Map<String, Double> mccThresholds = new HashMap<>();

        for (int i = 0; i < mccCount; i++) {
            String line = br.readLine();
            String[] parts = line.split(",");

            String mcc = parts[0].trim();
            double threshold = Double.parseDouble(parts[1].trim());

            mccThresholds.put(mcc, threshold);
        }

        /*
         * ---------------------------------------------------------
         * 4. Merchants
         * account_id,MCC
         * ---------------------------------------------------------
         */
        int merchantCount = Integer.parseInt(br.readLine().trim());

        Map<String, Merchant> merchants = new HashMap<>();

        for (int i = 0; i < merchantCount; i++) {
            String line = br.readLine();
            String[] parts = line.split(",");

            String accountId = parts[0].trim();
            String mcc = parts[1].trim();

            Double threshold = mccThresholds.get(mcc);

            if (threshold == null) {
                continue;
            }

            merchants.put(accountId, new Merchant(accountId, mcc, threshold));
        }

        int minimumTransactions = Integer.parseInt(br.readLine().trim());

        // 6. Number of events
        int eventCount = Integer.parseInt(br.readLine().trim());

        /*
         * charge_id -> Charge
         *
         * Required so that a later DISPUTE can find the
         * original transaction.
         */
        Map<String, Charge> charges = new HashMap<>();

        /*
         * If a dispute arrives before its charge, remember it.
         */
        Set<String> disputedChargeIds = new HashSet<>();

        for (int i = 0; i < eventCount; i++) {
            String line = br.readLine();

            if (line.startsWith("CHARGE,"))
            {
                processCharge(line, merchants, fraudCodes, charges, disputedChargeIds, minimumTransactions);
            }
            else if (line.startsWith("DISPUTE,"))
            {
                processDispute(line, merchants, charges, disputedChargeIds, minimumTransactions);
            }
        }

        List<String> fraudulentMerchants = new ArrayList<>();

        for (Merchant merchant : merchants.values()) {
            if (merchant.fraudulent) {
                fraudulentMerchants.add(merchant.accountId);
            }
        }

        //Lexicographical ordering
        Collections.sort(fraudulentMerchants);

        System.out.println(String.join(",", fraudulentMerchants));
    }

    /**
     * Process a CHARGE event.
     *
     * Format:
     *
     * CHARGE,charge_id,account_id,amount,code
     */
    private static void processCharge(String line, Map<String, Merchant> merchants, Set<String> fraudCodes,
            Map<String, Charge> charges, Set<String> disputedChargeIds,int minimumTransactions)
    {
        String[] parts = line.split(",");

        /*
         * parts[0] = CHARGE
         * parts[1] = charge_id
         * parts[2] = account_id
         * parts[3] = amount
         * parts[4] = code
         */
        String chargeId = parts[1].trim();
        String accountId = parts[2].trim();
        String code = parts[4].trim();

        Merchant merchant = merchants.get(accountId);

        if (merchant == null) {
            return;
        }

        boolean isFraudulent = fraudCodes.contains(code);

        /*
         * If a DISPUTE for this charge arrived before the
         * CHARGE, treat it as disputed immediately.
         */
        boolean isDisputed = disputedChargeIds.contains(chargeId);

        Charge charge = new Charge(chargeId,accountId,code,isFraudulent,isDisputed);
        charges.put(chargeId, charge);
        merchant.addTransaction(isFraudulent,isDisputed,minimumTransactions);
    }

    /*
     * Process a DISPUTE event.
     *
     * Format:
     * DISPUTE,charge_id
     */
    private static void processDispute(String line, Map<String, Merchant> merchants, Map<String, Charge> charges,
            Set<String> disputedChargeIds, int minimumTransactions)
    {

        String[] parts = line.split(",");
        String chargeId = parts[1].trim();

        // Look for the original charge.
        Charge charge = charges.get(chargeId);

        /*
         * Charge hasn't appeared yet.
         *
         * Remember the dispute so that when the charge arrives,
         * it will be treated as non-fraudulent.
         */
        if (charge == null)
        {
            disputedChargeIds.add(chargeId);
            return;
        }

        // Ignore duplicate disputes.
        if (charge.disputed)
            return;

        charge.disputed = true;

        /*
         * If the original transaction was fraudulent,
         * remove it from the merchant's fraudulent count.
         */
        if (charge.fraudulent) {
            Merchant merchant = merchants.get(charge.accountId);

            if (merchant != null) {
                merchant.disputeFraudulentTransaction(minimumTransactions);
            }
        }
    }

    // Parse comma-separated codes.
    private static Set<String> parseCodes(String line) {
        Set<String> result = new HashSet<>();

        if (line == null || line.trim().isEmpty()) {
            return result;
        }

        String[] parts = line.split(",");

        for (String part : parts) {
            result.add(part.trim());
        }

        return result;
    }
}
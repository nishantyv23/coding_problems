import java.util.*;

public class InvoiceReconciliation
{
    static class Invoice {
        String invoiceId;
        String dueDate;
        long amountDue;

        Invoice(String invoiceId, String dueDate, long amountDue) {
            this.invoiceId = invoiceId;
            this.dueDate = dueDate;
            this.amountDue = amountDue;
        }
    }

    static class Payment {
        String paymentId;
        long amount;
        String memo;

        Payment(String paymentId, long amount, String memo) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.memo = memo;
        }
    }

    public static String reconcile(String paymentString,List<String> invoiceStrings)
    {
        Map<String, Invoice> invoices = new HashMap<>();

        for (String invoiceString : invoiceStrings)
        {
            String[] parts = invoiceString.split(",");

            String invoiceId = parts[0].trim();
            String dueDate = parts[1].trim();
            long amountDue = Long.parseLong(parts[2].trim());

            invoices.put(invoiceId, new Invoice(invoiceId, dueDate, amountDue));
        }

        String[] paymentParts = paymentString.split(",", 3);

        String paymentId = paymentParts[0].trim();
        long paymentAmount = Long.parseLong(paymentParts[1].trim());

        String memo = paymentParts[2].trim();

        Payment payment = new Payment(paymentId, paymentAmount, memo);

        String prefix = "Paying off:";

        if (!payment.memo.startsWith(prefix)) {
            return "Unable to reconcile " + payment.paymentId;
        }

        String invoiceId = payment.memo.substring(prefix.length()).trim();

        Invoice invoice = invoices.get(invoiceId);

        if (invoice == null) {
            return "Unable to reconcile " + payment.paymentId + ": invoice " + invoiceId + " not found";
        }

        return payment.paymentId + " pays off " + payment.amount+ " for " + invoice.invoiceId + " due on "
                + invoice.dueDate;
    }

    public static void main(String[] args) {
        String payment = "payment5,1000,Paying off: invoiceC";
        List<String> invoices = Arrays.asList(
                "invoiceA,2024-01-01,100",
                "invoiceB,2024-02-01,200",
                "invoiceC,2023-01-30,1000"
        );

        System.out.println(reconcile(payment, invoices));
    }
}
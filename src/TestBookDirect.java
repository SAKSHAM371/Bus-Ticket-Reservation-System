import com.bus.dao.CustomerDaoImpl;

public class TestBookDirect {
    public static void main(String[] args) {
        try {
            CustomerDaoImpl dao = new CustomerDaoImpl();
            // Change the parameters to an existing bName and cusId
            String res = dao.bookTicket("Shatabdi Mumbai-Pune", 1, 2);
            System.out.println("Result: " + res);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

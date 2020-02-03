package example.com.solutvent.Model;

public class Request {
    private String id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private String plannerPhone;
    private String plannerEmail;
    private String plannerCompanyName;
    private String plannerPrice;
    private String plannerAddress;
    private String date;
    private String time;
    private String status;
    private String payment;
    private Long slot;

    public Request(){

    }

    public Request(String id, String customerName, String customerPhone, String customerEmail, String customerAddress, String plannerPhone, String plannerEmail, String plannerCompanyName, String plannerPrice, String plannerAddress, String date, String time, String status, String payment, Long slot) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.plannerPhone = plannerPhone;
        this.plannerEmail = plannerEmail;
        this.plannerCompanyName = plannerCompanyName;
        this.plannerPrice = plannerPrice;
        this.plannerAddress = plannerAddress;
        this.date = date;
        this.time = time;
        this.status = status;
        this.payment = payment;
        this.slot = slot;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getPlannerPrice() {
        return plannerPrice;
    }

    public void setPlannerPrice(String plannerPrice) {
        this.plannerPrice = plannerPrice;
    }

    public String getPlannerAddress() {
        return plannerAddress;
    }

    public void setPlannerAddress(String plannerAddress) {
        this.plannerAddress = plannerAddress;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlannerPhone() {
        return plannerPhone;
    }

    public void setPlannerPhone(String plannerPhone) {
        this.plannerPhone = plannerPhone;
    }

    public String getPlannerEmail() {
        return plannerEmail;
    }

    public void setPlannerEmail(String plannerEmail) {
        this.plannerEmail = plannerEmail;
    }

    public String getPlannerCompanyName() {
        return plannerCompanyName;
    }

    public void setPlannerCompanyName(String plannerCompanyName) {
        this.plannerCompanyName = plannerCompanyName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getSlot() {
        return slot;
    }

    public void setSlot(Long slot) {
        this.slot = slot;
    }
}

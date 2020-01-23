package example.com.solutvent.Model;

public class Request {
    private String id;
    private String organizerName;
    private String organizerPhone;
    private String organizerEmail;
    private String plannerPhone;
    private String plannerEmail;
    private String plannerCompanyName;
    private String date;
    private String time;
    private String status;
    private String payment;
    private Long slot;

    public Request(){

    }

    public Request(String id, String organizerName, String organizerPhone, String organizerEmail, String plannerPhone, String plannerEmail, String plannerCompanyName, String date, String time, String status, Long slot, String payment) {
        this.id = id;
        this.organizerName = organizerName;
        this.organizerPhone = organizerPhone;
        this.organizerEmail = organizerEmail;
        this.plannerPhone = plannerPhone;
        this.plannerEmail = plannerEmail;
        this.plannerCompanyName = plannerCompanyName;
        this.date = date;
        this.time = time;
        this.status = status;
        this.slot = slot;
        this.payment = payment;
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

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getOrganizerPhone() {
        return organizerPhone;
    }

    public void setOrganizerPhone(String organizerPhone) {
        this.organizerPhone = organizerPhone;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }

    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
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

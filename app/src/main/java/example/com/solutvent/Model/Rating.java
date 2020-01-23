package example.com.solutvent.Model;

public class Rating {
    private String userPhone;
    private String eventId;
    private String rateValue;
    private String comment;

    public Rating() {
    }

    public Rating(String userPhone, String eventId, String rateValue, String comment) {
        this.userPhone = userPhone;
        this.eventId = eventId;
        this.rateValue = rateValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

package example.com.solutvent.Model;

public class Event {
    private String name;
    private String image;
    private String description;
    private String price;
    private String booking;
    private String userContact;
    private String userEmail;
    private String location;
    private String date;
    private String time;
    private String menuId;
    private String companyName;


    public Event() {
    }

    public Event(String name, String image, String description, String price, String booking, String location, String userContact, String userEmail, String date, String time, String menuId, String companyName) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.price = price;
        this.booking = booking;
        this.userContact = userContact;
        this.userEmail = userEmail;
        this.location = location;
        this.date = date;
        this.time = time;
        this.menuId = menuId;
        this.companyName = companyName;
    }

    public String getBooking() { return this.booking; }

    public void setBooking(String booking) { this.booking = booking; }

    public String getUserContact() { return this.userContact; }

    public void setUserContact(String userContact) { this.userContact = userContact; }

    public String getLocation() { return this.location; }

    public void setLocation(String location) { this.location = location; }

    public String getDate() { return this.date; }

    public void setDate(String date) { this.date = date; }

    public String getTime() { return this.time; }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setTime(String time) { this.time = time; }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return this.price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMenuId() {
        return this.menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}

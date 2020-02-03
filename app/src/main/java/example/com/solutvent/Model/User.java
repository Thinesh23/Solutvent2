package example.com.solutvent.Model;

public class User {
    private String firstName;
    private String email;
    private String password;
    private String phone;

    private String status;
    private String isStaff;
    private String isPlanner;
    private String secureCode;
    private String companyName;
    private String companyImage;
    private String address;
    private String state;
    private String price;
    private String menuId;

    public User() {
    }

    public User(String firstName, String email, String password, String phone, String status, String isStaff, String isPlanner, String secureCode, String companyName, String companyImage, String address, String state, String price, String menuId) {
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.status = status;
        this.isStaff = isStaff;
        this.isPlanner = isPlanner;
        this.secureCode = secureCode;
        this.companyName = companyName;
        this.companyImage = companyImage;
        this.address = address;
        this.state = state;
        this.price = price;
        this.menuId = menuId;
    }

    public User(String firstName, String email, String password, String phone, String status, String isStaff, String isPlanner, String secureCode, String address, String state) {
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.status = status;
        this.isStaff = isStaff;
        this.isPlanner = isPlanner;
        this.secureCode = secureCode;
        this.address = address;
        this.state = state;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getCompanyImage() {
        return companyImage;
    }

    public void setCompanyImage(String companyImage) {
        this.companyImage = companyImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    public String getIsPlanner() {
        return isPlanner;
    }

    public void setIsPlanner(String isPlanner) {
        this.isPlanner = isPlanner;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}

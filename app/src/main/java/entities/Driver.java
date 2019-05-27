package entities;

public class Driver {

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getID() {
        return ID;

    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPhneNumber() {
        return phneNumber;
    }

    public void setPhneNumber(String phneNumber) {
        this.phneNumber = phneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    private String firstName;
    private String lastName;
    private String ID;
    private String phneNumber;
    private String email;
    private String creditCardNumber;
    private String password;


    public Driver(String firstName, String lastName, String ID, String phoneNumber, String email, String creditCardNumber, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ID = ID;
        this.phneNumber = phoneNumber;
        this.email = email;
        this.creditCardNumber = creditCardNumber;
        this.password = password;


    }

    public Driver() { }
}

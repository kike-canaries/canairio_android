package hpsaturn.pollutionreporter.models;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class PlayerItem {

    public String email;
    public String phone;
    public String name;

    public PlayerItem() {
    }

    public PlayerItem(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
//        return "name: "+name+" email: "+email+" phone: "+phone;
        return "name: "+name;
    }

}

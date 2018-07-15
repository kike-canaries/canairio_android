package hpsaturn.pollutionreporter.models;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class RecordItem {

    public String date;
    public String location;
    public String name;

    public RecordItem() {
    }

    public RecordItem(String name, String date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String email) {
        this.date = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String phone) {
        this.location = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "name: "+name;
    }

}

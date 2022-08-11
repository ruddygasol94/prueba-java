import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "phone_numbers")
public class PhoneNumber {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(unique = true)
    String number;

    @DatabaseField
    String number_type;

    public PhoneNumber() {
        // ORMLite needs a no-arg constructor
    }

    public int getId() {
        return this.id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber_type() {
        return number_type;
    }

    public void setNumber_type(String number_type) {
        this.number_type = number_type;
    }
}

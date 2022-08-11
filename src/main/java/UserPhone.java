import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_phone")
public class UserPhone {
    public final static String USER_ID_FIELD_NAME = "user_id";
    public final static String PHONE_ID_FIELD_NAME = "phone_id";

    @DatabaseField(generatedId = true)
    int id;

    // This is a foreign object which just stores the id from the User object in this table.
    @DatabaseField(foreign = true, columnName = USER_ID_FIELD_NAME)
    User user;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, columnName = PHONE_ID_FIELD_NAME)
    PhoneNumber phone;

    UserPhone() {
        // for ormlite
    }

    public UserPhone(User user, PhoneNumber post) {
        this.user = user;
        this.phone = post;
    }

    public PhoneNumber getPhone() {
        return phone;
    }
}

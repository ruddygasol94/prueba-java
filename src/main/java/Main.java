import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.BeanUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        String databaseUrl = "jdbc:postgresql://128.199.8.81:58522/prueba_java";

        try {
            ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
            ((JdbcConnectionSource) connectionSource).setUsername("admin");
            ((JdbcConnectionSource) connectionSource).setPassword("");

            Dao<User,String> userDao = DaoManager.createDao(connectionSource, User.class);
            Dao<PhoneNumber,Integer> phoneNumberDao = DaoManager.createDao(connectionSource, PhoneNumber.class);
            Dao<UserPhone, String> userPhoneDao = DaoManager.createDao(connectionSource, UserPhone.class);

            //TableUtils.createTableIfNotExists(connectionSource, User.class);
            //TableUtils.createTableIfNotExists(connectionSource, PhoneNumber.class);
            //TableUtils.createTableIfNotExists(connectionSource, UserPhone.class);

            get("/hello", (req, res) -> "Hello World");

            get("/contacts/:id", (req, res) -> {
              try {
                  final ObjectMapper mapper = new ObjectMapper();
                  User oFindUser = userDao.queryForId(req.params(":id"));

                  List<PhoneNumber> phones = new ArrayList<>();
                  QueryBuilder<UserPhone, String> upQb = userPhoneDao.queryBuilder();

                  upQb.where().eq("user_id", oFindUser.getId());
                  PreparedQuery<UserPhone> pqPN = upQb.prepare();
                  List<UserPhone> numbers = userPhoneDao.query(pqPN);
                  for (UserPhone pn: numbers) {
                      PhoneNumber oTemp = phoneNumberDao.queryForId(pn.getPhone().getId());
                      phones.add(oTemp);
                  }
                  oFindUser.setPhones(phones);

                  String jsonInString = mapper.writeValueAsString(oFindUser);

                  res.header("Content-Type", "application/json");
                  return jsonInString;
              } catch (Exception e) {
                  return "Error";
              }
            });

            get("/contacts", (req, res) -> {
                try {
                    List<User> users = userDao.queryForAll();
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final ObjectMapper mapper = new ObjectMapper();

                    for (User u:users) {
                        List<PhoneNumber> phones = new ArrayList<>();
                        QueryBuilder<UserPhone, String> upQb = userPhoneDao.queryBuilder();

                        upQb.where().eq("user_id", u.getId());
                        PreparedQuery<UserPhone> pqPN = upQb.prepare();
                        List<UserPhone> numbers = userPhoneDao.query(pqPN);
                        for (UserPhone pn: numbers) {
                            PhoneNumber oTemp = phoneNumberDao.queryForId(pn.getPhone().getId());
                            phones.add(oTemp);
                        }
                        u.setPhones(phones);
                    }

                    mapper.writeValue(out, users);

                    final byte[] data = out.toByteArray();

                    res.header("Content-Type", "application/json");
                    return new String(data);
                } catch (Exception e) {
                    return "Error";
                }
            });

            post("/contacts", (req, res) -> {
                res.header("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    User oUser = mapper.readValue(req.body(), User.class);

                    userDao.create(oUser);

                    for (int iCont = 0; iCont < oUser.getPhones().size(); iCont++) {
                        PhoneNumber oTemp = oUser.getPhones().get(iCont);

                        QueryBuilder<PhoneNumber, Integer> phQb = phoneNumberDao.queryBuilder();
                        // where the id matches in the post-id from the inner query

                        phQb.where().eq("number", oTemp.number);
                        PreparedQuery<PhoneNumber> pqPN = phQb.prepare();
                        List<PhoneNumber> numbers = phoneNumberDao.query(pqPN);

                        if (numbers.size() == 0) {
                            phoneNumberDao.create(oTemp);
                        } else {
                            oTemp = numbers.get(0);
                        }

                        if (oTemp.getId() > 0) {
                            userPhoneDao.create(new UserPhone(oUser, oTemp));
                        }
                    }

                    res.status(201);

                    return mapper.writeValueAsString(oUser);
                } catch (Exception e) {
                    Error error = new Error(e.getMessage(), 500);
                    res.status(500);
                    return mapper.writeValueAsString(error);
                }
            });

            patch("/contacts/:id", (req, res) -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    User oUser = mapper.readValue(req.body(), User.class);
                    User oFindUser = userDao.queryForId(req.params(":id"));

                    if (oUser.getName() != null && !oUser.getName().equals(oFindUser.getName())) {
                        oFindUser.setName(oUser.getName());
                    }

                    if (oUser.getLast_name() != null && !oUser.getLast_name().equals(oFindUser.getLast_name())) {
                        oFindUser.setLast_name(oUser.getLast_name());
                    }

                    if (oUser.getEmail() != null && !oUser.getEmail().equals(oFindUser.getEmail())) {
                        oFindUser.setEmail(oUser.getEmail());
                    }
                    userDao.update(oFindUser);
                    String jsonInString = mapper.writeValueAsString(oFindUser);

                    res.header("Content-Type", "application/json");
                    res.status(200);
                    return jsonInString;
                } catch (Exception e) {
                    res.status(500);
                    return "Error";
                }
            });

            get("/phones/:id", (req, res) -> {
                res.header("Content-Type", "application/json");
                try {
                    int id = Integer.parseInt(req.params(":id"));
                    PhoneNumber number = phoneNumberDao.queryForId(id);
                    ObjectMapper mapper = new ObjectMapper();

                    if (number != null) {
                        return mapper.writeValueAsString(number);
                    } else {
                        Error error = new Error("Phone not found", 404);
                        res.status(404);
                        return mapper.writeValueAsString(error);
                    }

                } catch (Exception e) {
                    res.status(500);
                    return "Error";
                }
            });

            delete("/phones/:id", (req, res) -> {
                res.header("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PhoneNumber oPhone = mapper.readValue(req.body(), PhoneNumber.class);
                    int id = Integer.parseInt(req.params(":id"));
                    PhoneNumber oFindPhone = phoneNumberDao.queryForId(id);

                    if (oFindPhone != null) {
                        phoneNumberDao.delete(oFindPhone);

                        res.status(200);
                        return mapper.writeValueAsString(oFindPhone);
                    } else {
                        Error error = new Error("Phone not found", 404);
                        res.status(404);
                        return mapper.writeValueAsString(error);
                    }
                } catch (Exception e) {
                    Error error = new Error(e.getMessage(), 500);
                    res.status(500);
                    return mapper.writeValueAsString(error);
                }
            });

            patch("/phones/:id", (req, res) -> {
                res.header("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PhoneNumber oPhone = mapper.readValue(req.body(), PhoneNumber.class);
                    int id = Integer.parseInt(req.params(":id"));
                    PhoneNumber oFindPhone = phoneNumberDao.queryForId(id);

                    if (oPhone.getNumber() != null && !oPhone.getNumber().equals(oFindPhone.getNumber())) {
                        oFindPhone.setNumber(oPhone.getNumber());
                    }

                    if (oPhone.getNumber_type() != null && !oPhone.getNumber_type().equals(oFindPhone.getNumber_type())) {
                        oFindPhone.setNumber_type(oPhone.getNumber_type());
                    }
                    phoneNumberDao.update(oFindPhone);
                    String jsonInString = mapper.writeValueAsString(oFindPhone);

                    res.status(200);
                    return jsonInString;
                } catch (Exception e) {
                    Error error = new Error(e.getMessage(), 500);
                    res.status(500);
                    return mapper.writeValueAsString(error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
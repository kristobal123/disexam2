package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.cj.protocol.Resultset;
import com.sun.javafx.scene.traversal.Algorithm;
import model.User;
import org.glassfish.jersey.client.JerseyWebTarget;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    Hashing hashwhatever = new Hashing();

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. FIXED
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
                //hasher password
            + hashwhatever.HashSalt(user.getPassword())
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }


  public static void deleteUser(int id) {

    //checking for Database connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Prepared Statement
    String sql = "DELETE FROM user WHERE id=" + id;

    dbCon.deleteUser(sql);

  }

  public static String loginUser(User user) {

    Hashing hashing = new Hashing();

    if (dbCon == null)
      dbCon = new DatabaseController();

    ResultSet resultSet;
    User newUser;
    String token = null;

    try {
      PreparedStatement loginUser = dbCon.getConnection().prepareStatement("SELECT * FROM user WHERE email = ? AND password = ?");
      loginUser.setString(1, user.getEmail());
      loginUser.setString(2, hashing.HashSalt(user.getPassword()));

      resultSet = loginUser.executeQuery();

      if (resultSet.next()) {
        newUser = new User(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("password"),
                resultSet.getString("email"));

        if (newUser != null) {
          try {
            Algorithm algorithm = Algorithm.HMAC256("Hemmelig");
            token = JWT.create()
                    .withClaim
                    .withIssuer("auth0")
                    .sign(algorithm);
          } catch (JWTCreattionException ex) {

          }finally {
            return token;
          }
          }
        } else {
        System.out.println("Could not found the user");
      }



    } catch (SQLException e) {
      e.printStackTrace();
    }
return"";
  }
}

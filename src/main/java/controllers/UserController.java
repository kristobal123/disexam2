package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.algorithms.Algorithm;
import com.mysql.cj.protocol.Resultset;
import model.User;
import org.glassfish.jersey.client.JerseyWebTarget;
import utils.Hashing;
import utils.Log;

import javax.xml.transform.Result;

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
    } else {
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

    //Checking Database Connectivity
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "SELCET * FROM user where email= " + user.getEmail() + " AND password= " + user.getPassword() + "";

    dbCon.loginUser(sql);

    //Here we do the query
    ResultSet resultSet = dbCon.query(sql);
    User userlogin;
    String token = null;

    try {
      //here we get the first object, because we only have one
      if (resultSet.next()) {
        userlogin = new User(
                resultSet.getInt("Id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("password"),
                resultSet.getString("email"));

        if (userlogin != null) {
          try {

            Algorithm algorithm = Algorithm.HMAC256("hemmelig");
            token = JWT.create()
                    .withClaim("userid", userlogin.getId())
                    .withIssuer("auth0")
                    .sign(algorithm);
          } catch (JWTCreationException exception) {
            //Invalid signing configuration / could not convert claims
            System.out.println(exception.getMessage());
          } finally {
            return token;
          }
        }
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
//Return null

    return "";

  }
}




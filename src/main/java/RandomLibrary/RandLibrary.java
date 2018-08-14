package RandomLibrary;

import com.github.javafaker.Faker;
import logger.Logger;


import java.util.Locale;
import java.util.Random;

public class RandLibrary {
    Faker faker = new Faker();
    Logger logger=new Logger();
    /**
     * +     * @param
     * +     * @auther : Ankit Patel
     * +     * @lastModifiedBy:
     * +
     */
    // Random First Name
    public String firstName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fname = faker.name().firstName();
        return fname;

    }

    // Random Last Name
    public String LastName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String lname = faker.name().lastName();
        return lname;
    }

    // Random Full Name
    public String fullName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullName = faker.name().fullName();
        return fullName;
    }

    // Random Email
    public String eMail() {
        Faker faker = new Faker(new Locale("en-IND"));
        String email = faker.internet().emailAddress();
        return email;

    }

    // Random user Name
    public String userName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullName = faker.name().username();
        return fullName;
    }

    // Random Mobile Number
    public String number() {
        int number = faker.number().numberBetween(75, 98);
        String mobileNumber = faker.number().digits(8);
        String phoneNumber = number + mobileNumber;
        return phoneNumber;

    }


    // Random Age
    public String AgeAdult() {
        int age = faker.number().numberBetween(22, 40);
        Integer intInstance = new Integer(age);
        String numberAsString = intInstance.toString();
        return numberAsString;
    }


    //Random Birthday
    public String Birthday() {
        int day = faker.number().numberBetween(01, 28);
        int month = faker.number().numberBetween(01, 12);
        int year = faker.number().numberBetween(1960, 1990);
        String birth = day + "/" + month + "/" + year;
        return birth;
    }

    //Random DebitCardNo
    public String DebitCardNo() {
        String card = faker.business().creditCardNumber();
        return card;
    }

    public String ExpiryDate() {
        int expiryDay = faker.number().numberBetween(01,12);
        int expiryMonth = faker.number().numberBetween(25,30);
        String ExpiryDate = expiryDay+"/"+expiryMonth;
        return ExpiryDate;
    }

    public String cvvNo() {
        String cvvNo = faker.number().digits(3);
        return cvvNo;
    }

    public String Country() {
        String country = faker.address().country();
        return country;
    }

    public String state() {
        Faker faker = new Faker(new Locale("en-IND"));
        String state = faker.address().state();
        return state;
    }

    public String city() {
        Faker faker = new Faker(new Locale("en-IND"));
        String city = faker.address().cityName();
        return city;
    }

    public String postcode() {
        Faker faker = new Faker(new Locale("en-IND"));
        String zipcode = faker.address().zipCode();
        return zipcode;
    }

    public String street() {
        Faker faker = new Faker(new Locale("en-IND"));
        String Street = faker.address().streetName();
        return Street;
    }

    public String emoji() {
        String emoji = faker.slackEmoji().emoji();
        return emoji;
    }

    public String lorem() {
        String lorem = faker.lorem().sentence();
        return lorem;
    }

    public String maritalStatus() {
        String status = faker.demographic().maritalStatus();
        return status;
    }

    public String gender() {
        String sex = faker.demographic().sex();
        return sex;
    }

    public String fullAddress() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullAddress = faker.address().fullAddress();
        return fullAddress;
    }

    public String internetDomain() {
        Faker faker = new Faker(new Locale("en-IND"));
        String domainName = faker.internet().domainName();
        return domainName;
    }

    public String GSTNo() {
        int state = faker.number().numberBetween(01,35);
        String PAN = faker.number().digits(4);
        String gst = state+"AQWCV"+PAN+"A"+"1Z2";
        return gst;
    }

    public String PANNo() {
        int state = faker.number().numberBetween(01,35);
        String PAN = faker.number().digits(4);
        String panNo = "AQWCV"+PAN+"A";
        return panNo;
    }

    public String companyName() {
        String companyName = faker.company().name();
        return companyName;
    }

    public String password() {
        String password = faker.internet().password();
        return password;
    }

    public String IDNo() {
        String idNo = faker.idNumber().valid();
        return idNo;
    }

    public String passport() {
       String passport = faker.number().digits(7);
       return passport;
    }

    public String houseNo() {
        String streetNo = faker.number().digits(3);
        return streetNo;
    }

    public String bankACNo() {
        String bankACNo = faker.number().digits(13);
        return bankACNo;
    }

    public String cardname() {
        String cardname = faker.business().creditCardType();
        return cardname;
    }
    public String RandomNo(int length) {
        String SALTCHARS = "1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public String RandomNoAlpha(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public String RandomAlpha(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }


}


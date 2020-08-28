package randomlibrary;

import com.github.javafaker.Faker;
import logger.TesboLogger;


import java.util.Locale;
import java.util.Random;

public class RandLibrary {
    Faker fakers = new Faker();
    String indText="en-IND";
    Random rnd = new Random();

    TesboLogger tesboLogger =new TesboLogger();

    /**
     *
     * @return
     */
    // Random First Name
    public String firstName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().firstName();

    }

    /**
     *
     * @return
     */
    // Random Last Name
    public String lastName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().lastName();
    }

    /**
     *
     * @return
     */
    // Random Full Name
    public String fullName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().fullName();
    }

    /**
     *
     * @return
     */
    // Random Email
    public String eMail() {
        Faker faker = new Faker(new Locale(indText));
        return faker.internet().emailAddress();

    }

    /**
     *
     * @return
     */
    // Random user Name
    public String userName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().username();
    }

    /**
     *
     * @return
     */
    // Random Mobile Number
    public String number() {
        int number = fakers.number().numberBetween(75, 98);
        String mobileNumber = fakers.number().digits(8);
        return number + mobileNumber;

    }

    /**
     *
     * @return
     */

    // Random Age
    public String ageAdult() {
        int age = fakers.number().numberBetween(22, 40);
        return String.valueOf(age);
    }

    /**
     *
     * @return
     */
    //Random Birthday
    public String birthday() {
        int day = fakers.number().numberBetween(01, 28);
        int month = fakers.number().numberBetween(01, 12);
        int year = fakers.number().numberBetween(1960, 1990);
        return day + "/" + month + "/" + year;
    }

    /**
     *
     * @return
     */
    //Random DebitCardNo
    public String debitCardNo() {
        return fakers.business().creditCardNumber();

    }

    /**
     *
     * @return
     */
    public String expiryDate() {
        int expiryDay = fakers.number().numberBetween(01,12);
        int expiryMonth = fakers.number().numberBetween(25,30);
        return expiryDay+"/"+expiryMonth;
    }

    /**
     *
     * @return
     */
    public String cvvNo() {
        return fakers.number().digits(3);
    }

    /**
     *
     * @return
     */
    public String country() {
        return fakers.address().country();
    }

    /**
     *
     * @return
     */
    public String state() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().state();
    }

    /**
     *
     * @return
     */
    public String city() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().cityName();
    }

    /**
     *
     * @return
     */
    public String postcode() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().zipCode();
    }

    /**
     *
     * @return
     */
    public String street() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().streetName();
    }

    /**
     *
     * @return
     */
    public String emoji() {
        return fakers.slackEmoji().emoji();
    }

    /**
     *
     * @return
     */
    public String lorem() {
        return fakers.lorem().sentence();
    }

    /**
     *
     * @return
     */
    public String maritalStatus() {
        return fakers.demographic().maritalStatus();
    }

    /**
     *
     * @return
     */
    public String gender() {
        return fakers.demographic().sex();
    }

    /**
     *
     * @return
     */
    public String fullAddress() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().fullAddress();
    }

    /**
     *
     * @return
     */

    public String internetDomain() {
        Faker faker = new Faker(new Locale(indText));
        return faker.internet().domainName();
    }

    /**
     *
     * @return
     */
    public String gstNo() {
        int state = fakers.number().numberBetween(01,35);
        String pan = fakers.number().digits(4);
        return state+"AQWCV"+pan+"A"+"1Z2";
    }

    /**
     *
     * @return
     */
    public String panNo() {
        String pan = fakers.number().digits(4);
        return "AQWCV"+pan+"A";
    }

    /**
     *
     * @return
     */
    public String companyName() {
        return fakers.company().name();
    }

    /**
     *
     * @return
     */
    public String password() {
        return fakers.internet().password();
    }

    /**
     *
     * @return
     */
    public String idNo() {
        return fakers.idNumber().valid();
    }

    /**
     *
     * @return
     */
    public String passport() {
       return fakers.number().digits(7);
    }

    /**
     *
     * @return
     */
    public String houseNo() {
        return fakers.number().digits(3);
    }

    /**
     *
     * @return
     */
    public String bankACNo() {
        return fakers.number().digits(13);
    }

    /**
     *
     * @return
     */
    public String cardname() {
        return fakers.business().creditCardType();
    }

    /**
     *
     * @param length
     * @return
     */
    public String randomNo(int length) {
        String saltChars = "1234567890";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < length) {
            int index = (rnd.nextInt() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();

    }

    /**
     *
     * @param length
     * @return
     */
    public String randomAlpha(int length) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < length) {
            int index =(rnd.nextInt() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    /**
     *
     * @return
     */
    // Random Mailinator Mail
    public String randomEmailWithMailinator() {
        Faker faker = new Faker(new Locale(indText));
        String fname = faker.name().firstName();
        return fname+"@mailinator.com";

    }

    /**
     *
     * @return
     */
    // Random Yopmail Mail
    public String randomEmailWithYopmail() {
        Faker faker = new Faker(new Locale(indText));
        String fname = faker.name().firstName();
        return fname+"@yopmail.com";

    }


}


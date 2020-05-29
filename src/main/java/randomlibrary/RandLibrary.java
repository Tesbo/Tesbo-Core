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
     * +     * @param
     * +     * @auther : Ankit Patel
     * +     * @lastModifiedBy:
     * +
     */
    // Random First Name
    public String firstName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().firstName();

    }

    // Random Last Name
    public String lastName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().lastName();
    }

    // Random Full Name
    public String fullName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().fullName();
    }

    // Random Email
    public String eMail() {
        Faker faker = new Faker(new Locale(indText));
        return faker.internet().emailAddress();

    }

    // Random user Name
    public String userName() {
        Faker faker = new Faker(new Locale(indText));
        return faker.name().username();
    }

    // Random Mobile Number
    public String number() {
        int number = fakers.number().numberBetween(75, 98);
        String mobileNumber = fakers.number().digits(8);
        return number + mobileNumber;

    }


    // Random Age
    public String ageAdult() {
        int age = fakers.number().numberBetween(22, 40);
        return String.valueOf(age);
    }


    //Random Birthday
    public String birthday() {
        int day = fakers.number().numberBetween(01, 28);
        int month = fakers.number().numberBetween(01, 12);
        int year = fakers.number().numberBetween(1960, 1990);
        return day + "/" + month + "/" + year;
    }

    //Random DebitCardNo
    public String debitCardNo() {
        return fakers.business().creditCardNumber();

    }

    public String expiryDate() {
        int expiryDay = fakers.number().numberBetween(01,12);
        int expiryMonth = fakers.number().numberBetween(25,30);
        return expiryDay+"/"+expiryMonth;
    }

    public String cvvNo() {
        return fakers.number().digits(3);
    }

    public String country() {
        return fakers.address().country();
    }

    public String state() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().state();
    }

    public String city() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().cityName();
    }

    public String postcode() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().zipCode();
    }

    public String street() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().streetName();
    }

    public String emoji() {
        return fakers.slackEmoji().emoji();
    }

    public String lorem() {
        return fakers.lorem().sentence();
    }

    public String maritalStatus() {
        return fakers.demographic().maritalStatus();
    }

    public String gender() {
        return fakers.demographic().sex();
    }

    public String fullAddress() {
        Faker faker = new Faker(new Locale(indText));
        return faker.address().fullAddress();
    }

    public String internetDomain() {
        Faker faker = new Faker(new Locale(indText));
        return faker.internet().domainName();
    }

    public String gstNo() {
        int state = fakers.number().numberBetween(01,35);
        String pan = fakers.number().digits(4);
        return state+"AQWCV"+pan+"A"+"1Z2";
    }

    public String panNo() {
        String pan = fakers.number().digits(4);
        return "AQWCV"+pan+"A";
    }

    public String companyName() {
        return fakers.company().name();
    }

    public String password() {
        return fakers.internet().password();
    }

    public String idNo() {
        return fakers.idNumber().valid();
    }

    public String passport() {
       return fakers.number().digits(7);
    }

    public String houseNo() {
        return fakers.number().digits(3);
    }

    public String bankACNo() {
        return fakers.number().digits(13);
    }

    public String cardname() {
        return fakers.business().creditCardType();
    }
    public String randomNo(int length) {
        String saltChars = "1234567890";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < length) {
            int index = (rnd.nextInt() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();

    }


    public String randomAlpha(int length) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < length) {
            int index =(rnd.nextInt() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }
    // Random Mailinator Mail
    public String randomEmailWithMailinator() {
        Faker faker = new Faker(new Locale(indText));
        String fname = faker.name().firstName();
        return fname+"@mailinator.com";

    }

    // Random Yopmail Mail
    public String randomEmailWithYopmail() {
        Faker faker = new Faker(new Locale(indText));
        String fname = faker.name().firstName();
        return fname+"@yopmail.com";

    }


}


package RandomLibrary;

import com.github.javafaker.Faker;


import java.util.Locale;
import java.util.Random;

public class RandLibrary {
    Faker faker = new Faker();

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
        System.out.println("First Name: " + fname);
        return fname;

    }

    // Random Last Name
    public String LastName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String lname = faker.name().lastName();
        System.out.println("Last Name: " + lname);
        return lname;
    }

    // Random Full Name
    public String fullName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullName = faker.name().fullName();
        System.out.println("Full Name: " + fullName);
        return fullName;
    }

    // Random Email
    public String eMail() {
        Faker faker = new Faker(new Locale("en-IND"));
        String email = faker.internet().emailAddress();
        System.out.println("Email: " + email);
        return email;

    }

    // Random user Name
    public String userName() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullName = faker.name().username();
        System.out.println("User Name: " + fullName);
        return fullName;
    }

    // Random Mobile Number
    public String number() {
        int number = faker.number().numberBetween(75, 98);
        String mobileNumber = faker.number().digits(8);
        String phoneNumber = number + mobileNumber;
        System.out.println("Mobile number: " + phoneNumber);
        return phoneNumber;

    }


    // Random Age
    public String AgeAdult() {
        int age = faker.number().numberBetween(22, 40);
        Integer intInstance = new Integer(age);
        String numberAsString = intInstance.toString();
        System.out.println("Age: " + numberAsString);
        return numberAsString;
    }


    //Random Birthday
    public String Birthday() {
        int day = faker.number().numberBetween(01, 28);
        int month = faker.number().numberBetween(01, 12);
        int year = faker.number().numberBetween(1960, 1990);
        String birth = day + "/" + month + "/" + year;
        System.out.println("Birthday : " + birth);
        return birth;
    }

    //Random DebitCardNo
    public String DebitCardNo() {
        String card = faker.business().creditCardNumber();
        System.out.println("Card Number: " + card);
        return card;
    }

    public String ExpiryDate() {
        int expiryDay = faker.number().numberBetween(01,12);
        int expiryMonth = faker.number().numberBetween(25,30);
        String ExpiryDate = expiryDay+"/"+expiryMonth;
        System.out.println("Expiry Date: " + ExpiryDate);
        return ExpiryDate;
    }

    public String cvvNo() {
        String cvvNo = faker.number().digits(3);
        System.out.println("CVV Number: " + cvvNo);
        return cvvNo;
    }

    public String Country() {
        String country = faker.address().country();
        System.out.println("Country Name: " + country);
        return country;
    }

    public String state() {
        Faker faker = new Faker(new Locale("en-IND"));
        String state = faker.address().state();
        System.out.println("State Name: " + state);
        return state;
    }

    public String city() {
        Faker faker = new Faker(new Locale("en-IND"));
        String city = faker.address().cityName();
        System.out.println("City Name: " + city);
        return city;
    }

    public String postcode() {
        Faker faker = new Faker(new Locale("en-IND"));
        String zipcode = faker.address().zipCode();
        System.out.println("Pincode Number: " + zipcode);
        return zipcode;
    }

    public String street() {
        Faker faker = new Faker(new Locale("en-IND"));
        String Street = faker.address().streetName();
        System.out.println("Street: " + Street);
        return Street;
    }

    public String emoji() {
        String emoji = faker.slackEmoji().emoji();
        System.out.println("emoji: " + emoji);
        return emoji;
    }

    public String lorem() {
        String lorem = faker.lorem().sentence();
        System.out.println("lorem: " + lorem);
        return lorem;
    }

    public String maritalStatus() {
        String status = faker.demographic().maritalStatus();
        System.out.println("Marital Status: " + status);
        return status;
    }

    public String gender() {
        String sex = faker.demographic().sex();
        System.out.println("Gender: " + sex);
        return sex;
    }

    public String fullAddress() {
        Faker faker = new Faker(new Locale("en-IND"));
        String fullAddress = faker.address().fullAddress();
        System.out.println("full Address: " + fullAddress);
        return fullAddress;
    }

    public String internetDomain() {
        Faker faker = new Faker(new Locale("en-IND"));
        String domainName = faker.internet().domainName();
        System.out.println("Domain Name: " + domainName);
        return domainName;
    }

    public String GSTNo() {
        int state = faker.number().numberBetween(01,35);
        String PAN = faker.number().digits(4);
        String gst = state+"AQWCV"+PAN+"A"+"1Z2";
        System.out.println("GST Number: "+gst);
        return gst;
    }

    public String PANNo() {
        int state = faker.number().numberBetween(01,35);
        String PAN = faker.number().digits(4);
        String panNo = "AQWCV"+PAN+"A";
        System.out.println("PAN Number: "+panNo);
        return panNo;
    }

    public String companyName() {
        String companyName = faker.company().name();
        System.out.println("Company Name: "+companyName);
        return companyName;
    }

    public String password() {
        String password = faker.internet().password();
        System.out.println("Password: "+password);
        return password;
    }

    public String IDNo() {
        String idNo = faker.idNumber().valid();
        System.out.println("ID Number: "+idNo);
        return idNo;
    }

    public String passport() {
       String passport = faker.number().digits(7);
        System.out.println("Passport Number: "+"A"+passport);
        return passport;
    }

    public String houseNo() {
        String streetNo = faker.number().digits(3);
        System.out.println("House Number: "+streetNo);
        return streetNo;
    }

    public String bankACNo() {
        String bankACNo = faker.number().digits(13);
        System.out.println("Bank Account Number: "+bankACNo);
        return bankACNo;
    }

    public String cardname() {
        String cardname = faker.business().creditCardType();
        System.out.println("Credit Card Type: "+cardname);
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
        System.out.println("Random no : " + saltStr);
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
        System.out.println("Random no Alpha : " + saltStr);
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
        System.out.println("Random alpha : " + saltStr);
        return saltStr;
    }


}


package student.com;

public class Student {

    private String firstName;

    private String lastName;

    private String regNumber;

    private String gender;

    public Student(String firstName, String lastName, String regNumber, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.regNumber = regNumber;
        this.gender = gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Student{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", regNumber='" + regNumber + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}


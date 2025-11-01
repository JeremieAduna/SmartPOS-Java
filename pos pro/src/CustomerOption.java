public class CustomerOption {


    public int id;
    public String name;

    CustomerOption(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

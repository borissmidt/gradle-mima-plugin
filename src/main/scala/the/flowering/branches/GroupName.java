package the.flowering.branches;

public class GroupName {
    final String group;
    final String name;

    public GroupName(String group, String name) {
        this.group = group;
        this.name = name;
    }

    String asString(){
        return String.join(":",group,name);
    }
}
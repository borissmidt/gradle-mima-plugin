package the.flowering.branches.mima;

public class GroupName {
    final String group;
    final String name;

    public GroupName(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public GroupNameVersion withVersion(String version) {
        return new GroupNameVersion(
                group,
                name,
                version
        );
    }

    String asString(){
        return String.join(":",group,name);
    }
}
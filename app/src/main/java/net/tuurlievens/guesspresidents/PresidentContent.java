package net.tuurlievens.guesspresidents;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Add ImageViews
// TODO: Use real data

public class PresidentContent {

    public static List<President> list = getPresidentData();

    public static final List<President> getPresidentData() {
        return new ArrayList<>(Arrays.asList(
            new President(0,"Washington","Description",1,2,"https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Gilbert_Stuart_-_George_Washington_-_Google_Art_Project.jpg/374px-Gilbert_Stuart_-_George_Washington_-_Google_Art_Project.jpg"),
            new President(1,"Lincoln","Description",2,3,"https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Gilbert_Stuart_-_George_Washington_-_Google_Art_Project.jpg/374px-Gilbert_Stuart_-_George_Washington_-_Google_Art_Project.jpg"),
            new President(2,"Garfield","Description",3,4,"https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Gilbert_Stuart_-_George_Washington_-_Google_Art_Project.jpg/374px-Gilbert_Stuart_-_George_Washington_-_Google_Art_Project.jpg")
        ));
    }

    public static class President {
        public final int id;
        public final String name;
        public final String descr;
        public final int birthyear;
        public final int deathyear;
        public final String imageurl;

        President(int id, String name, String descr, int birthyear, int deathyear, String imageurl) {
            this.id = id;
            this.name = name;
            this.descr = descr;
            this.birthyear = birthyear;
            this.deathyear = deathyear;
            this.imageurl = imageurl;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}

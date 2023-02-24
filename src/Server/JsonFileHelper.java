package Server;

import Models.Group;
import Models.GroupType;
import Models.Message;
import Models.User;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class JsonFileHelper {

    private final String diretoryName;
    private final Gson jsonHelper;

    public JsonFileHelper(String diretoryName) throws IOException {
        Files.createDirectories(Paths.get(diretoryName));

        this.jsonHelper = configGson();

        this.diretoryName = diretoryName;
    }

    private Gson configGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.serializeNulls();

        return gsonBuilder.create();
    }

    public synchronized <T> void serialize(String filename, T obj) throws IOException {
        try (Writer writer = new FileWriter(diretoryName + filename + ".json")) {
            writer.write(jsonHelper.toJson(obj));
        }
    }

    //Rever, nao esta completamente funcional
//    public synchronized <T> T deserializeObject(String filename) throws IOException {
//        if (!new File(diretoryName + filename + ".json").exists()) return null;
//
//        return jsonHelper.fromJson(new String(Files.readAllBytes(Paths.get(diretoryName + filename + ".json"))), new TypeToken<T>() {
//        }.getType());
//    }

    public synchronized <T> List<T> deserializeArray(String filename, Class<T[]> type) throws IOException {
        if (!new File(diretoryName + filename + ".json").exists()) return new ArrayList<>();

        String jsonString = new String(Files.readAllBytes(Paths.get(diretoryName + filename + ".json")));

        if (jsonString.isEmpty())
            return new ArrayList<>();

        return Arrays.asList(jsonHelper.fromJson(jsonString, type));
    }

    public synchronized boolean addUser(User user) throws IOException {
        Set<User> users = new HashSet<>(getUsers());

        if (!users.add(user)) return false;

        serialize("users", users);
        return true;
    }

    public synchronized boolean updateUser(User user) throws IOException {
        Set<User> users = new HashSet<>(getUsers());

        users.remove(user);
        users.add(user);

        serialize("users", users);
        return true;
    }

    public synchronized void updateUsers(ArrayList<User> users) throws IOException {
        serialize("users", new HashSet<>(users));
    }

    public synchronized User getUser(String username, String password) throws IOException {
        ArrayList<User> users = new ArrayList<>(getUsers());
        return users.stream().filter(user -> user.getPassword().equals(password) && user.getUsername().equals(username)).findFirst().orElse(null);
    }

    public synchronized User getUser(String username) throws IOException {
        ArrayList<User> users = new ArrayList<>(getUsers());
        return users.stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);
    }

    public synchronized ArrayList<User> getUsers() throws IOException {
        return new ArrayList<>(deserializeArray("users", User[].class));
    }

    public synchronized ArrayList<Message> getMessages() throws IOException {
        return new ArrayList<>(deserializeArray("messages", Message[].class));
    }

    public synchronized ArrayList<Message> getMessagesUser(String username) throws IOException {
        return new ArrayList<>(getMessages()).stream()
                .filter(message -> message.getTo().equals(username) || message.getFrom().equals(username))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public synchronized boolean addMessage(Message message) throws IOException {
        ArrayList<Message> messages = new ArrayList<>(getMessages());
        messages.add(message);
        serialize("messages", messages);
        return true;
    }


    public synchronized boolean addGroup(Group group) throws IOException {
        ArrayList<Group> groups = new ArrayList<>(getGroups());

        if (!groups.add(group)) return false;

        serialize("groups", groups);
        return true;
    }

    public synchronized ArrayList<Group> getGroups() throws IOException {
        return new ArrayList<>(deserializeArray("groups", Group[].class));
    }

    private static class LocalDateTimeSerialization implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public JsonElement serialize(LocalDateTime date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(date));
        }

        @Override
        public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }

    private static final class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDate) throws IOException {
            jsonWriter.value(formatter.format(localDate));
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), formatter);
        }
    }

}

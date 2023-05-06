package bot.deskort.commands.custom;

import bot.deskort.commands.Command;
import bot.utilities.Option;
import bot.utilities.formatters.JavaFormatter;
import bot.utilities.requests.JsonBody;
import bot.utilities.requests.SimpleResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.http.client.fluent.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AICommand extends Command{
    private static final String OPENAI_KEY = "";
    private static final String AI21_KEY = "";
    private static final String GPT_MODEL_3_5 = "gpt-3.5-turbo-0301";
    private static final String OPENAI_MODELS_URL = "https://api.openai.com/v1/models";
    public static final String AI21_J2_API = "https://api.ai21.com/studio/v1/j2-";

    private String phrase;
    private MessageChannelUnion channel;
    public AICommand(String... aliases){
        super(aliases);
        description = "Generates AI responses for given inputs.\n" +
                "AI21 has 3 Jurassic-2 completion models:\n `large`, `grande` and `jumbo`.\n" +
                "AI21 has 2 Jurassic-2 instruction models:\n `grande-instruct` and `jumbo-instruct`.\n" +
                "Each consecutive model is more complex but also slower.\n" +
                "To get more information about a model use: ai21 `model`.\n" +
                "All of J2 models support several non-English languages, including:\n" +
                "Spanish, French, German, Portuguese, Italian, Dutch.\n" +
                "*Tasks*:\n" +
                "`paraphrase` - convey the same meaning using different words (Wordtune)\n" +
                "`gec` - grammatical error correction (Wordtune API)\n" +
                "`improvements` - enhance your writing experience, elevate your final product\n" +
                "`summarize` - summarize your text (the same engine that powers Wordtune read)\n";
        usage = "ai21 `model` `phrase`\n" +
                "ai21 grande-instruct `phrase`\n" +
                "ai21 `model`\n" +
                "ai21 `task` `phrase`\n" +
                "ai21 paraphrase `phrase`\n" +
                "ai21 gec `phrase`\n" +
                "openai models\n";
    }

    @Override
    protected void executeImpl(String commandName, MessageReceivedEvent message, String... args){
        if(args.length == 0){
            return;
        }
        if(message != null){
            channel = message.getChannel();
        }

        if(commandName.equals("ai21")){
            String modelOrTask = args[0];
            if(args.length == 1){
                respondWithVariantsDescription(modelOrTask);
                return;
            }

            //this switch block is meant to validate the input parameters and distinguish tasks from models
            boolean isModel = false;
            AITask task = null;
            switch (modelOrTask){
                case "large":
                case "grande":
                case "jumbo":
                case "grande-instruct":
                case "jumbo-instruct":
                    isModel = true;
                    break;
                case "paraphrase":
                    task = AITask.PARAPHRASE;
                    break;
                case "gec":
                    task = AITask.GRAMMATICAL;
                    break;
                case "improvements":
                    task = AITask.TEXT_IMPROVEMENTS;
                    break;
                case "summarize":
                    task = AITask.SUMMARIZE;
                    break;
                default:
                    actions.sendEmbed(
                            channel,
                            createInfoEmbed("Model or task failed", "Unknown model or task")
                    );
                    break;
            }
            boolean isTask = task != null;
            if(!isModel && !isTask){
                return;
            }
            //attempt to format code if requested
            boolean isCode = args.length == 3 && args[2].startsWith("-");

            phrase = args[1];

            actions.sendEmbed(channel, createInfoEmbed("Command issued", ""));
            if(isTask){
                Request request = Request.Post(task.getEndpoint())
                        .addHeader("Authorization", "Bearer " + AI21_KEY)
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json");
                JsonBody params = adaptJsonBodyToTask(task);
                Option<SimpleResponse> maybeResponse = SimpleResponse.performRequest(request, params);
                if(!maybeResponse.isSome()){
                    actions.sendEmbed(
                            channel,
                            createInfoEmbed("No response for task", "Request returned no data")
                    );
                    return;
                }

                SimpleResponse response = maybeResponse.unwrap();
                if(response.code != 200){
                    actions.sendEmbed(
                            channel,
                            createInfoEmbed(String.valueOf(response.code), response.body)
                    );
                    return;
                }

                String content = extractRelevantContentFromResponse(task, response);
                actions.sendEmbed(channel, createInfoEmbed(task.toString(), content));
            }else{
                String url = AI21_J2_API + modelOrTask + "/complete";
                Request request = Request.Post(url)
                        .addHeader("Authorization", "Bearer " + AI21_KEY)
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json");
                JsonBody paramsBody = JsonBody.body()
                        .addPair("prompt", phrase)
                        .addPair("maxTokens", 512);

                Option<SimpleResponse> maybeResponse = SimpleResponse.performRequest(request, paramsBody);
                if(!maybeResponse.isSome()){
                    actions.sendEmbed(
                            channel,
                            createInfoEmbed("No response for completion", "Request returned no data")
                    );
                    return;
                }
                SimpleResponse response = maybeResponse.unwrap();
                System.out.println(response.body);
                if(response.code != 200){
                    actions.sendEmbed(
                            channel,
                            createInfoEmbed(String.valueOf(response.code), response.body)
                    );
                    return;
                }
                JSONObject jsonResponse = JSONObject.parseObject(response.body);
                JSONArray arr = jsonResponse.getJSONArray("completions");
                JSONObject firstEl = arr.getJSONObject(0);
                JSONObject data = firstEl.getJSONObject("data");
                String text = data.getString("text");
                if(isCode){
                    text = formatBrokenCode(text, args[2].substring(1));
                }
                actions.sendEmbed(channel, createInfoEmbed("Response", text));
            }
        }

        else if(commandName.equals("openai")){
            if (args[0].equals("models")){
                List<Model> models = retrieveOpenAIModels();
                String modelsAsString = modelsToString(models);
                actions.messageChannel(channel, modelsAsString);
            }
            return;
        }
    }

    private String formatBrokenCode(String text, String lang){
        int index = text.indexOf("Copy code");
        if(index == -1){
            index = text.indexOf("import");
            if(index < 0){
                index = text.indexOf("class");
            }
            if(index < 0){
                index = 0;
            }
        }else{
            index += 9;
        }
        return "```" + lang + '\n' +
                JavaFormatter.format(text.substring(index)) + "```";
    }

    private String extractRelevantContentFromResponse(AITask task, SimpleResponse response){
        StringBuilder str = new StringBuilder();
        JSONObject json = JSONObject.parseObject(response.body);
        switch (task){
            case PARAPHRASE:
                JSONArray suggestionsArr = json.getJSONArray("suggestions");
                for (int i = 0; i < suggestionsArr.size(); i++){
                    JSONObject textObj = suggestionsArr.getJSONObject(i);
                    String text = textObj.getString("text");
                    str.append(i + 1).append(". ").append(text).append('\n');
                }
                break;
            case GRAMMATICAL:
                JSONArray correctionsArr = json.getJSONArray("corrections");
                int size = correctionsArr.size();
                if(size == 0){
                    str.append("No corrections available");
                    break;
                }
                str.append(phrase);
                for (int i = 0; i < size; i++){
                    JSONObject suggestionObj = correctionsArr.getJSONObject(i);
                    String suggestion = suggestionObj.getString("suggestion");
                    String original = suggestionObj.getString("originalText");
                    int indexInOriginal = str.indexOf(original);
                    str.replace(indexInOriginal, original.length() + indexInOriginal, suggestion);
                }
                break;
            case TEXT_IMPROVEMENTS:
                JSONArray improvementsArr = json.getJSONArray("improvements");
                if(improvementsArr.size() == 0){
                    str.append("No improvements available");
                    break;
                }
                JSONObject suggestionObj = improvementsArr.getJSONObject(0);
                JSONArray suggestions = suggestionObj.getJSONArray("suggestions");
                for (int i = 0; i < suggestions.size(); i++){
                    String alternative = suggestions.getString(i);
                    str.append(i + 1).append(". ").append(alternative).append('\n');
                }
                break;
            case SUMMARIZE:
                String summary = json.getString("summary");
                str.append(summary);
                break;
        }
        return str.toString();
    }

    private JsonBody adaptJsonBodyToTask(AITask task){
        JsonBody paramsBody = JsonBody.body();
        switch (task){
            case PARAPHRASE:
                paramsBody
                        .addPair("text", phrase)
                        .addPair("style", "general"); //general, casual, formal, long, short
                break;
            case GRAMMATICAL:
                paramsBody.addPair("text", phrase);
                break;
            case TEXT_IMPROVEMENTS:
                paramsBody.addPair("text", phrase);
                paramsBody.addPair(
                        "types",
                        Arrays.asList("fluency", "vocabulary/variety", "clarity/conciseness"));
                break;
            case SUMMARIZE:
                paramsBody
                        .addPair("source", phrase)
                        .addPair("sourceType", "TEXT");
                break;
        }
        return paramsBody;
    }

    private void respondWithVariantsDescription(String modelName){
        switch (modelName.toLowerCase()){
            case "large":
                actions.sendEmbed(
                        channel,
                        createInfoEmbed("Large", "Designed for fast responses, this model can be fine-tuned to optimize performance for relatively simple tasks.")
                );
                break;
            case "grande":
                actions.sendEmbed(
                        channel,
                        createInfoEmbed("Grande", "This model offers enhanced text generation capabilities, making it well-suited to language tasks with a greater degree of complexity.")
                );
                break;
            case "jumbo":
                actions.sendEmbed(
                        channel,
                        createInfoEmbed("Grande", "As the largest and most powerful model in the Jurassic series, J2-Jumbo is an ideal choice for the most complex language processing tasks and generative text applications.")
                );
                break;
            case "grande-instruct":
                actions.sendEmbed(
                        channel,
                        createInfoEmbed("Grande-Instruct", "Optimized for generating precise text based on minimal context, which makes it ideal for use cases such as chatbots and other conversational interfaces.")
                );
                break;
            case "jumbo-instruct":
                actions.sendEmbed(
                        channel,
                        createInfoEmbed("Jumbo-Instruct", "Offers superior language understanding and response generation capabilities, making it ideal for advanced conversational interface needs.")
                );
                break;
            default:
                actions.sendEmbed(channel, createInfoEmbed("Unknown", "Unknown variant."));
                break;
        }
    }

    private static MessageEmbed createInfoEmbed(String title, String desc){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription(desc);
        return embed.build();
    }

    private String modelsToString(List<Model> models){
        StringBuilder str = new StringBuilder();
        for(Model m : models){
            str.append("id = `").append(m.id).append('`').append(" ");
            str.append("owned_by = `").append(m.owned_by).append('`').append('\n');
        }
        return str.toString();
    }

    private static List<Model> retrieveOpenAIModels(){
        Request request = Request.Get(OPENAI_MODELS_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + OPENAI_KEY);

        SimpleResponse response = SimpleResponse.performRequest(request).expect("Simple response is null");
        System.out.println(response);
        JSONObject jsonBody = JSONObject.parseObject(response.body);
        JSONArray modelsArr = jsonBody.getJSONArray("data");
        List<Model> models = new ArrayList<>(modelsArr.size());
        for (int i = 0; i < modelsArr.size(); i++){
            JSONObject jsonModel = modelsArr.getJSONObject(i);
            String id = jsonModel.getString("id");
            String owned_by = jsonModel.getString("owned_by");
            models.add(new Model(id, owned_by));
        }
        return models;
    }

    @Override
    protected void finalize() {
        //release sockets
    }

}

enum AITask{
    PARAPHRASE("paraphrase"),
    GRAMMATICAL("gec"),
    TEXT_IMPROVEMENTS("improvements"),
    SUMMARIZE("summarize");

    private final String endpoint;
    private static final String AI21_J2_TASK = "https://api.ai21.com/studio/v1/";

    AITask(String endpoint){
        this.endpoint = AI21_J2_TASK + endpoint;
    }

    public String getEndpoint(){
        return endpoint;
    }

}

class Model{
    public String id;
    public String owned_by;

    public Model(String id, String owned_by){
        this.id = id;
        this.owned_by = owned_by;
    }

    @Override
    public String toString(){
        return "Model{" +
                "id='" + id + '\'' +
                ", owned_by='" + owned_by + '\'' +
                '}';
    }
}
package bot.textprocessors;

import bot.core.Bot;
import bot.utilities.Hasher;
import bot.utilities.jda.Actions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BallsDex extends TextProcessor{
    private static final Hint hints = Hint.NON_COUNTRIES_ONLY;
    private Actions actions;
    private MessageReceivedEvent message;
    @Override
    boolean consume(String content, MessageReceivedEvent message){
        if(message.getAuthor().getIdLong() != 999736048596816014L){
            return false;
        }
        this.message = message;
        actions = Bot.getActions();
        List<Message.Attachment> attachments = message.getMessage().getAttachments();
        if(content.startsWith("A wild country") && attachments.size() == 1){
            Message.Attachment image = attachments.get(0);
            retrieveCountry(image);
        }
        return false;
    }

    private void retrieveCountry(Message.Attachment image){
        File tempDir = new File("tmp");
        tempDir.mkdir();
        File temp = new File("tmp/temp_file");
        temp.delete();
        MessageChannelUnion channel = message.getChannel();
        try{
            temp = image.getProxy().downloadToFile(temp).get(6, TimeUnit.SECONDS);
        }catch (InterruptedException | ExecutionException | TimeoutException e){
            actions.messageChannel(channel, "Job timed out");
            return;
        }
        byte[] bytes;
        try{
            bytes = Files.readAllBytes(temp.toPath());
        }catch (IOException e){
            System.err.println("IO error on read");
            return;
        }
        try{
            String hash = Hasher.hashBytes(bytes, Hasher.choose("sha256"));
            if(sha256ToCountry.containsKey(hash)){
                CountryBall country = sha256ToCountry.get(hash);
                displayAccordingToHints(country);
            }else{
                actions.messageChannel(channel, hash);
            }
        }catch (IllegalStateException exc){
            exc.printStackTrace();
        }finally{
            temp.delete();
            tempDir.deleteOnExit();
        }
    }

    private void displayAccordingToHints(CountryBall country){
        if(hints == Hint.ALL){
            actions.messageChannel(message.getChannel(), country.name);
        }else if(hints == Hint.COUNTRIES_ONLY && country.isCountry){
            actions.messageChannel(message.getChannel(), country.name);
        }else if(hints == Hint.NON_COUNTRIES_ONLY && !country.isCountry){
            actions.messageChannel(message.getChannel(), country.name);
        }
    }

    private static final HashMap<String, CountryBall> sha256ToCountry = new HashMap<String, CountryBall>(){{
        put("4009c9cdaf67bc52f80de09fb9af1a8c73ef4a44dd49528083960208ae736515", new CountryBall("Libya"));
        put("4e543e840ea610bf0595e131d15f496a09d4380c2966af869823fc4fa94f18ca", new CountryBall("Iceland"));
        put("dad196c7b6bf0858acfee7197d18a2145bd1f7cb5c7c114bdd286623b715c3b3", new CountryBall("Macedon", false));
        put("b8be8494387819784c4b4a9aa104e21a013208ca4ecc4f568f2ad252fd7af9c5", new CountryBall("Bahrain"));
        put("f42d4919733c535603f868fe9e724fb90a811eac053bb1337cf487461391c13d", new CountryBall("Turkey"));
        put("f7922ba7af01b6e91bd3fd6b8ffa6b90fc18b71fda2181ccce2e5d37c66a7ccd", new CountryBall("Papua New Guinea"));
        put("67ddc9c3e55bcfddf150d1c622b97510b909df8186013d1b17fdf575794f0f6a", new CountryBall("South Yemen"));
        put("556915ccf6005a6fc52603716b6769468f1e776bc8850b8a781f76f31c3e44cd", new CountryBall("Mauritania"));
        put("1b66ceff565dc29bd8852c9ccd361b6eeb27a991af31e617fbb893e16d541599", new CountryBall("Namibia"));
        put("7b3eb27b63e4a842c0f2a42450c871dec32ec2f389ab6d8a834774e8c2509d08", new CountryBall("Romania"));
        put("e6335666317b7ccc7574ccbdfb7ad7d01da5b07ca1cc32b2da5d6dab867442c8", new CountryBall("Tunisia"));
        put("f001639d68c235b40df569f25e5c9aa23b0c43ccf256716cf95afbf5beb778fa", new CountryBall("Indonesia"));
        put("6770c2511a8657576b079730d147640b651c1a57220f220931881f828f7a88a8", new CountryBall("Liechtenstein"));
        put("1446e3eb3a6c65ae50ee1df835177896c9d1f6c57c7a19dd3b828870be63a24f", new CountryBall("Central African Republic"));
        put("1ede0c08fd83aa47cb1febac9b230ab837904cc3e66af4fbd04169180878c28e", new CountryBall("Spanish Empire", false));
        put("0bb34120487c620f8fe8e68fbcdc6b943d9e8144054958478b6621ba6b69783e", new CountryBall("Ecuador"));
        put("84eb8a6746b0faf2f0dcc523f79cc0ab029cbb80b17cc80fd1c00422f0865e49", new CountryBall("Mongolia"));
        put("b8859fd081e3fb6a7817257461612a670ad2fd09418de99a8f6b8b18bb3d4751", new CountryBall("Fiji"));
        put("d2fd130c68b3d756fe1929ef75fedeb1d93cdf954f319975596492ac4f7b3591", new CountryBall("Free France", false));
        put("ba4c2acbcb16a827524a5e0185677aee5c7767967c98ba5e8b8ac7ed60644a65", new CountryBall("North Korea"));
        put("12a4cd5d7a924e77aa22f54a3927d0ca9b9011ee42fd91079c8f580dbfd07ae6", new CountryBall("Bhutan"));
        put("272c0c7d92c4eeaf625c93905052734fc6f8b7159f925312d691aa86176a30bc", new CountryBall("Arab League", false));
        put("477a18e8864157d264fa84a930f789903a3edef18f296045447eeaf30c8ab7bb", new CountryBall("Japan"));
        put("cc3b1e22ab7bea263cfdd73d808b5e511b28b6394ede4b8f141f1168fb25fb29", new CountryBall("Kuwait"));
        put("47f815c9c0107971d1c576ac316ab71fa0751195788e40b4c6b719f2280fdad2", new CountryBall("Prussia", false));
        put("cb863aa537b0c29009d66b27f93b21affa5f3a11acc8b5e4ca73f358bbdfa030", new CountryBall("Cameroon"));
        put("bd7c55d5b488c7ebdd69289655b831f93fff3604b6d07531b64e6f8405813b48", new CountryBall("Bangladesh"));
        put("98df02f2f7aa85bf0250f3cdf36cdfecefe1d557304af6e2d6bd0657b1b59d71", new CountryBall("Iran"));
        put("70add967b5903fe7de502f74535d7973d5c2f2374e9e02e9dff07bfa7c15fb5d", new CountryBall("Somaliland", false));
        put("daaacc93a560f20cc5e0ab91559c400e31c1da66cd8fb30ac4cadaed1c56c58d", new CountryBall("Norway"));
        put("3104ea03b3686cf4b7e42ff90311635da1bc6677cac31193f02d01d2dcd889a9", new CountryBall("Belarus"));
        put("da4bf8deccd76b08fdc3a345e9168a4663ae0657f5d84112564ab7532ed34870", new CountryBall("Slovenia"));
        put("2a178f4673dbefcebdbc1ea289619fc64090fa29bc9d5933ce0310a1e742e87a", new CountryBall("French Indochina", false));
        put("9c2fcac82f9fc96dd3202bb6ba2861706336c9df0a7d6ec466511a836a625c01", new CountryBall("Khiva", false));
        put("3cd8a2f5868d802f327a57b5aaaf5fbb32bffcedbbc581e6cd42038c09395d88", new CountryBall("Polish Underground State", false));
        put("468af5334eaeaccf6109858d50172a9e7828334955d076cb2b3a18bb9ccfe424", new CountryBall("Hudson Bay Company", false));
        put("70c2f7fff136af44c6c959f000fdc032cffdfd99fa5519def0135e494cde9592", new CountryBall("Greece"));
        put("ea8e52706583a8f039e4432d99179d1a1fcd62f9b46a36fe7037713ca7b51464", new CountryBall("Ghana"));
        put("0e8bd2483f824b761f7958c447d4062cd76ff957e5a9d8cbcd3140843cedbedc", new CountryBall("Republican Spain", false));
        put("56705cc9059c4f8d271fc25b84560ce0a0f23a2f66697c29d22434a385c92299", new CountryBall("Gambia"));
        put("55ef73f7aedac98432995ff395f4c7b3f157c3a246a0a74c82ac6b45bdb3eaa3", new CountryBall("Somalia"));
        put("38764908c3ada936933bd5811391f3c52318b5a528b04b9cbc955b1ac49da252", new CountryBall("East Germany", false));
        put("580f8ca338984254100bb0d862bb642f1ed6579c4e9a3740c391357143e5a49a", new CountryBall("Ancient Egypt", false));
        put("98f1351f6efc911d946868b9ec2f65b50d888d75ee616e866c31b936c9f541ed", new CountryBall("Weimar Republic", false));
        put("3d6f8422f6df24c572d310b3cf7e33126fd4612302fe08b27181c8a05e0e49cb", new CountryBall("Free City of Danzig", false));
        put("e212461aa5ff81c04288031748ea986c89da1c7c3e11ecdf7b007ef14c6270ed", new CountryBall("Tanzania"));
        put("c0700aef6ce5a89bbe7a732e53a4182c793d32ef3e5f33a16bdd2d372b33293b", new CountryBall("Vichy France", false));
        put("cf106c11d2ef86ae4ecbf0f6fbb392ea20ada8699260e13be7560d75d82ee256", new CountryBall("Nicaragua"));
        put("4557561286c741c82e407c04766eb30906391c62e3b613eb53c98d8c5157ab02", new CountryBall("Ottoman Empire", false));
        put("c1a74b258379b9abdeff7d695903d4b797ffda505f1ba271418f962d0f92f3de", new CountryBall("Burkina Faso"));
        put("de3a7a02a3fc40d7c146d2473abad35651393cba3fb7b92126b880e5e96c7095", new CountryBall("Haiti"));
        put("f8a828eda65df64ae99e176804405cde118386037d760b5aba6718f851cec537", new CountryBall("Kingdom of Egypt", false));
        put("ad18826ed2cbeb827fcae488e9ad49fa3939bddb8b857db51e3d9d7a7b23da29", new CountryBall("San Marino"));
        put("c2ab37fd8cf3e876c827954262f17ce6f653c8a706a9fed3cd7ae3528e794221", new CountryBall("Mozambique"));
        put("beae51c83f794d96b5dcc0e2a8fc2221e29d05f44eaf4d29468603b955f786ee", new CountryBall("South Sudan"));
        put("6fa3250b0b8290a126c18f8fefdc64b466a19f390f84cb20d992f040dcd17569", new CountryBall("Western Sahara", false));
        put("8ffd38b1108247def80897e235fc400c1ae80675e7973eb70b72e8f261b73633", new CountryBall("Xiongnu", false));
        put("41d6db9dcc51b8af777e2a8424fadd517776fa820ec415e813f8bbd02f746e6f", new CountryBall("Lithuania"));
        put("dd481c6aaffe6822567382c27972b8dfab749efb87899c20ef79d9945a791ca4", new CountryBall("Luxembourg"));
        put("8085085743a0a5e94618494eb6656333806ca3046da05c5a8a17c8cfc757bf7f", new CountryBall("Paris Commune", false));
        put("0ace14dbafb217b4042333650eff6f2143dcf41acbde7ce6042645bc25731aaf", new CountryBall("DR Congo"));
        put("49b8423bd5a80b95e54e6195d9eb7e5478397558f5e04f2813eb5e7649b6569b", new CountryBall("Montenegro"));
        put("0ae63cf8d927b2f536bdda52a02a422cd933be3f4a7e670b4057503ffaeda373", new CountryBall("Congo Free State", false));
        put("19b6e64f409abcc30512370500c498f5cc67decc28d429bb17f5e98b56b1334e", new CountryBall("Mongolia"));
        put("1ddd998e96f430c242890fb170a835492718f85f9f756a97c99138eea5c3f2ab", new CountryBall("Netherlands"));
        put("e4c356aec3b6456b04801993aff282c699b49cb80a1d13347febbbe1f0c57a3c", new CountryBall("Moldova"));
        put("f5470a93b1970aa66f587487480db0a11ecd551db58ff6871a35d99cca509126", new CountryBall("Mexico"));
        put("79a9608801046e04d4731a4b267f488fc4b557858a78d736f9e74754db5b706f", new CountryBall("Panama"));
        put("68d4de381cda164d8de436378c9a70158157eaa3eaca5a4f85e5df7f93d57530", new CountryBall("Francoist Spain", false));
        put("c331ff1e37e9a4f4e9c8798380adb457cf863e314a63317854185eac86c68a0d", new CountryBall("Nepal"));
        put("079616ad6dbb3b40750ceb6f141d7f35e777a152d0e81757faa5c71d0e936b95", new CountryBall("Egypt"));
        put("876a81454138f99a67b12561f2abb3eb42a2d5546914bf8022e0a75cdca77276", new CountryBall("Niger"));
        put("d63a3cb0b6525d524c33627a0feb3ccc10062d24058dce24143d6eb8a172a726", new CountryBall("Nigeria"));
        put("12a84b8bb940ab1dca36befb1b70310e5c2893cff093aa85b881c74f277d55c8", new CountryBall("Guyana"));
        put("5b1e055755f4896bcc843bb8fc4d946efa8c9a5495943be0fabb8c98c1e60965", new CountryBall("Serbia"));
        put("3b4eff2c6831e94b37818c7194e603bd7231dce09b3348624977f031a409783c", new CountryBall("Lebanon"));
        put("d1aeedfcbfce75f9b6ad47692552dca5348bfabebdf1c592c81d081fe5be5e62", new CountryBall("Liberia"));
        put("e893018de831dacfb55238a6730a2403c3c72011c6b4b8a9d443acb6a882bf27", new CountryBall("Portugal"));
        put("51fefc4909766cea9aa82ec171d99283d662d3eefcd424b8c4d04b1e10cb76fc", new CountryBall("Manchukuo", false));
        put("3fa690d8c6156e1c12161a9b84c3329e8083506416fdbaa1b346baad83dcd30a", new CountryBall("Bahamas"));
        put("57af1d91525b1549b16d0b35a981e93c730cffe0bf1fad3df6f4084fd2fd0779", new CountryBall("Latvia"));
        put("0c2f14738a0d13a8543cfc4c057374566430c48c7eeb057bbe46ec47eeadf560", new CountryBall("Austria"));
        put("fc687f056057324999fc0ae0280f4adbba06acbbecb3bec4af9e7ad02b8547ed", new CountryBall("Kiribati"));
        put("a61e7bb88ab2a90f6ac48b69f5caabb53c39e27265fa067340fed7848066f09a", new CountryBall("Byelorussian Soviet Socialist Republic", false));
        put("a9b4b408a5ada5eab8d81f7b2f73eaa9d423ce601c67fc0e269145ce583b5786", new CountryBall("Denmark"));
        put("218b5070313db9ea61e672f9db570ea5b21824fb050d618411dc6ddab0de2ac1", new CountryBall("Australia"));
        put("0b86c627b4ff89b3b66fd1a8580dd3ed73034a904dd89d9c18f89772c22fc86b", new CountryBall("Finland"));
        put("ae2c51aaccfaacd3e976c28be1a57ec61bf5e35d6ccd3c8313809754245b9d8b", new CountryBall("Antarctica", false));
        put("a0cadd7b2ae324dd0af26706800d89a9271b3f4f871e8a73576e4b4862981d4f", new CountryBall("Chile"));
        put("0e638938383e09978955086d261546b967595dd75a8dfdc36d9b9a7bd075659e", new CountryBall("Cuba"));
        put("0d826f8099f71f68df2791a260a84e762ab930f69af7bc54cf9d79c280beea38", new CountryBall("Babylon", false));
        put("8af812c84010fbda8a4e3651f251e21fc50c3a59df87f1cb13a1e5bf55f55610", new CountryBall("Paraguay"));
        put("b5965691e1d4e2ebddad7937481649ff474f19d5c8b839d1226d50e328dfd8cf", new CountryBall("Slovakia"));
        put("abb9b6214a6809e62e5bc99bded5c5836a78d7a3da0491490461b9d9d5d7a356", new CountryBall("Botswana"));
        put("878fef36d34fc630fdd575779fbb10810919f815f1c88d53e6fea68bd3700555", new CountryBall("Portuguese Empire", false));
        put("e364e41b6ef50ac19205e01164f441d551720569790d091d35e41402b58c1b65", new CountryBall("Ethiopia"));
        put("d0eada761cce6f5df6801aae6040832f5fe919517ad1005a7c2a1e3de56bc8b1", new CountryBall("Uganda"));
        put("d3b8cafaf7b20f94377dd57735441aedfb3a0600e47c4725bc646b4cceb1c9c0", new CountryBall("Chad"));
        put("2b70b38ccf83a9852e2f04a01c84c7c824389160a11d113bc49693bf2ffbd76b", new CountryBall("Bosnia and Herzegovina"));
        put("f38aef2c96584f971873010a76895444a17b9b70b5b8c00f997153c72095a11f", new CountryBall("Saudi Arabia"));
        put("432961b6c31ee3a5f4e282dd3d2373eaa4ac36f6d26340576affed5d4073c361", new CountryBall("Uruguay"));
        put("cdca4d8efa997a036416f5c02240016b41a66d5f99567821c64258ffad38f1a0", new CountryBall("Morocco"));
        put("d4756ab25dad3ed336d91e634c39ae746b8664072e33535cacfb213177b70403", new CountryBall("Kyrgyzstan"));
        put("0ca4cb9676d5ef15da3262290a6ae898debcdb297a3b47f4d7ec51f5a872de56", new CountryBall("Syria"));
        put("5e6a3d050a8601cfbb87f463956df32e2efa896e25d5e5b3a3de365ace927fe3", new CountryBall("Madagascar"));
        put("c197f17a46d5127cd85f859129ee6c8481161039969a0b350f35da863f2cd1a1", new CountryBall("Bulgaria"));
        put("4d8ab63c276b4c92999fbb44ce6abda5850e3d99b61bbd68c3d43aa035afed53", new CountryBall("Brazil"));
        put("4b4fc4d3f294a99a5f6a61bcf9abe49fe745df992f6ab6c3eaf7c72386890275", new CountryBall("Peru"));
        put("a46781f1d9a0ee186dc2e0f4516daa8ad25908f2dfe46f9fe4dd0f93341eaede", new CountryBall("Laos"));
        put("c1f08b86e437992a8ef757446c567c381050cdc25f908a0a759e09b1525459eb", new CountryBall("Tannu Tuva", false));
        put("6760c5821c67b4616f4c9f691e48fcc0d3cc96165d61ef983bb39c923d0eba93", new CountryBall("Burundi"));
        put("569aad76849fdb4f8246f8a0b2d3ad38c9e94ce1d99ead95eb3001a704662036", new CountryBall("Seychelles"));
        put("db38f964fef52a2379aa9d98ac426305486a7cf2a499accf9ff253d720d62ca4", new CountryBall("NATO", false));
        put("2ba1536ef258bb6b581effc092b952f0b3d0e51c0f0217cb92b3d48b2ba1b8a1", new CountryBall("Albania"));
        put("ea459feb12a511fbfb5dfe82d6f4cef0baca5213bf7ed1b76104004827488f40", new CountryBall("Honduras"));
        put("40ba17f5979c4acb78ce20ecfa19a568af5c452a08049ae8fba7a775a261b55a", new CountryBall("Zimbabwe"));
        put("51847a17cdc5b0dd4af932488cfbb08c70f7524ec5a251cfde864ed8ebf75517", new CountryBall("Suriname"));
        put("0440b8fe047258562705c094ad7d931ff2a658ab96efde00a50ce01e8d66c716", new CountryBall("Qatar"));
        put("a780ba4e0df2eb3336628159d166552c5e2593b67a16f710d4f4f06a614dda6f", new CountryBall("British Empire", false));
        put("e31120a67a19bf62cb8cb38de6b43f0b3a63367f4a89ddc13f4e53edb2e69a87", new CountryBall("Afghanistan"));
        put("a8fdc5fbb087ed91fd3bfb406f31567c0aa9c569167e7be9bda1dfc9aa2a4780", new CountryBall("Cyprus"));
        put("19be3a7c29857a6fcf3d83ef61dfbb0aa9ccca7be5613a0333beef033938322b", new CountryBall("UAE"));
        put("8aaa53f37c34aaa745bf3846c87fd0cd14239ce5a2b4ee779d8dac952e30d06c", new CountryBall("Turkmenistan"));
        put("a0abd7214d9d5efa5412e45df35fe40552ba0b6864d6ccc3cbfcdd2216c0f0d3", new CountryBall("Tuvalu"));
        put("76aca5302dabc5d5e8e6486934cfd128ca543c958cabe345c66517b40b0b648c", new CountryBall("New Zealand"));
        put("e7e4038a56b1df9f46481eae5b0886a21c3fc9715f0b351acb33a40e16a58dde", new CountryBall("South Vietnam", false));
        put("e47fc1d03931679658867f7536cd85b12fae90304fd3132496aae758b9a3e304", new CountryBall("Uzbekistan"));
        put("6cc3b3620a5ddb522a7cfff7f39ec47a87287b65d6e7231385fd23ca48c675f7", new CountryBall("Maldives"));
        put("9d136fe291e8dde31951546d4d5f3c6aef31489d4b3c4ec3d99bd0037ebf4e67", new CountryBall("Taiwan"));
        put("bbf3538c014d2bd05d2946bf7c240cdf7b7da38ddc4076a7c88f3652231c11ee", new CountryBall("Armenia"));
        put("1bd72b48f7bf2f377e135f403e2bd739c6d2b378c4389615b8155683b47db6f7", new CountryBall("Ceylon"));
        put("f36cffd57999c54f908e5f8746950e88d606477b74a4dc024404428e65cd6708", new CountryBall("Ancient Sparta", false));
        put("5bd4fb18e5eaf54d7ec2204bce934ee9e08a6fe65abc96d77d0e073e0f7be999", new CountryBall("Gabon"));
        put("4c1e8545588b3a1b7a7df8c7d5e963fe272257c1780e01aaa7d580cdb91c99b7", new CountryBall("Ivory Coast"));
        put("a728fa44b3b28fe66379ad915f3641319894cd947685133d51f22c73ccc9c37c", new CountryBall("North Macedonia"));
        put("79b1d1890bcc49cf1e1da80afe8b0ee2f1c5b2d2d78a0b495337926ffbed9bf6", new CountryBall("Sierra Leone"));
        put("48b6e0ef5cd12fb6c8c149c203b6054ed94a8d962aeb3b0286658179aee313a7", new CountryBall("Angola"));
        put("3bfb780065f9e0f107ba4256dc2b9cdf9e713ed63229ce085f0706388444398d", new CountryBall("Yemen"));
        put("69565efc881297363bb26fe9256b16f135492669db5c2fec29821e010c04653f", new CountryBall("Djibouti"));
        put("2a055b7c8af51255610789af51db39219fe7fd20f375bbb75f2c27c3179d5311", new CountryBall("African Union", false));
        put("5168d8573984a2b5987dd5f49ce2fdeabe8c0ec5a599f4b1d7bcc459de6b7fc8", new CountryBall("Palestine"));
        put("3b1923afe2c1bb75dbea3c5e202a929f145d35b19072b23ff27d76c9a8b9eafa", new CountryBall("Malta"));
        put("742c1eedbc9f798cb4a627c182f11566dbc131621b736226733e05047abf4960", new CountryBall("West Germany", false));
        put("1327fcaf019a8a419b6abee15dae4efc4f9673ff4f192342f8dd2dd7fe3616e7", new CountryBall("Singapore"));
        put("f22782104b72a3f1b693219c639b76239149169aabee7da8205f7c3c55a57b0a", new CountryBall("Czechia"));
        put("b45f1ffcbb778bc7627ebdbf4474171a5e6a356d04c827ed082ac67214956a87", new CountryBall("Georgia"));
        put("889d8779030209a7b846a980bb41c43733bd13e5805b549f47d0c8aa9f964972", new CountryBall("Croatia"));
        put("334f609cc0ccbd94a4e65f2bcc7b67da46b357366f579d09e8325b13f2c034f4", new CountryBall("Hejaz", false));
        put("f9b3ed92ac415c0f82e4d4087fbd00118869a1f57bf59a8408fce04ee2d5ea2c", new CountryBall("Pakistan"));
        put("e438e45f711d535673eedf4226a5145b3ef78a1648981c9b20715bbf782e6aa7", new CountryBall("Zhou", false));
        put("8230e2d1c52bd872b1c2123b2dff9e9a696d9e4cf458fe04086158ca698f89f7", new CountryBall("Azerbaijan"));
        put("f44fbcc67221d7fe6bddc0a4c0299c1f65a8792d54c675e5ca9f472049980f82", new CountryBall("Napoleonic France", false));
        put("ffba90fb07e74e2a5cdf70320e6a8dcd88fc55829985e400e196b965d2efa556", new CountryBall("Kosovo"));
        put("8b1baca27a54500df33cc07fde1a56aeb5cce04f67ecf5870b48132731ac5019", new CountryBall("Kingdom of Greece", false));
        put("849b1af5cdab82f370440ef86b4e4023dfdd531f865c16a2c86790c09ae99120", new CountryBall("Belgium"));
        put("bbb6d799414a569314f065f4f7f7498e4cbb92864f80f6b7a5323fe2acbb2d38", new CountryBall("Eritrea"));
        put("1fdff3b6465637b69579194e29b61e7a71a1e89eb0394ce2639419dd786660d5", new CountryBall("Congo"));
        put("ad24f1f31d9a2eb58c77b7b88d0db3e36e5f38652feabfe7d20fdffac068d68a", new CountryBall("European Union", false));
        put("3e8c232e9e80fde61d7b4941151b0571087193ea609d7a0d6fde9edcf067930e", new CountryBall("Ancient Athens", false));
        put("1018ae4168ddb7caab26b78bb5324d27b6a2f66349f68e9c1460ff0ebd84de23", new CountryBall("Hungary"));
        put("2d3e8ed16c2ab8900050766f5e213def83e546f0e20402c030dbb701d5f43ea6", new CountryBall("Yugoslavia"));
        put("6db835453e23a6487a73b242898d7406273c2702e277e94baf606e3de9e9e5c1", new CountryBall("Sri Lanka"));
        put("89bfc6e413245b1c5d20309d6388bb56c22ea11f1297109777b770671f34d239", new CountryBall("Germany"));
        put("789d29d18fff79b707bff71750c4a908c754b0558005bfac733cc31104b841a5", new CountryBall("Bolivia"));
        put("06d1c1e21580402a3f040837d2d3d7e237b885c2b8ed1951fdce0217605897e9", new CountryBall("Cambodia"));
        put("04dc4f128ff755fc91998333f4ced237b9ef6105a4f48e81ea892816f60d54a0", new CountryBall("El Salvador"));
        put("54508312e91af089169cd9f8734c6127ad3a9a4f6e382df54b91aadbeda11408", new CountryBall("South Korea"));
        put("0ec689bec5d54d5209a3db7852d9a6c2ff0519e4f4b5d23ef136b8e72909d2da", new CountryBall("Gaul", false));
        put("afef637eb1306a5bc4eee8fb5162447cba6c3390408d2d0c47008ae9a6feea7a", new CountryBall("Switzerland"));
        put("031ce02b2b8568211d577e077142bb3f85e328a51097d8746989c0efb603c8c2", new CountryBall("Senegal"));
        put("d6d1d93a2a8d48e47a0b58e9504291c6af9d34656b4a45c623821e14f825b3f8", new CountryBall("Germania", false));
        put("7ff7239506b4bedb3e44b7fb484032191f079499d5d6c10a6448d45fbc728ad2", new CountryBall("League of Nations", false));
        put("b790b664c0b1d0ebc89fa13d65869deffe94253728303e4c758c3cbe03d6a0ec", new CountryBall("Thailand"));
        put("3448f83b11999f617979c6af969ea7a5947655a7f0c03ef18bd764d7f8c5ade3", new CountryBall("Jordan"));
        put("44f3d26690adece540debccf901d297b3b74945e3019cb2cbf5cad26b6eaabce", new CountryBall("Zambia"));
        put("a4159e43256aa367b4db53577dab436b63819cf9d7774a7208dae90f341fb5b1", new CountryBall("Argentina"));
        put("a6a0317ceec213569a6df27b9dddf2a5b0e0e27b6891fa27befca05ebcf164ae", new CountryBall("Oman"));
        put("25a0404c823b8ef82fb264a2696bb3232e23e3320b4ed8b314be505eb83f5bc9", new CountryBall("Sweden"));
        put("c685ec4fdcb85bb38f910236ce3c54ceeccde4786a937a4b7a19ab206b706b28", new CountryBall("Mayan Empire", false));
        put("a0a7dfc60286331c4c7e6270fdac27d2777511c3b49c993826a261958bf2343d", new CountryBall("Iraq"));
        put("b0ac67b46ad49b36beac2dae5f8fb11d934d32ab57532a794be062a3be40c856", new CountryBall("Venezuela"));
        put("9690967adbefce7a853e100c0416506ea1475a1f1ae05ce87bfe6feb9ed5dd70", new CountryBall("Guatemala"));
        put("b709a4d313ba96c0b70d3144790c5386fc1a836e2b0463de6b6b9fb75f0b04ef", new CountryBall("Estonia"));
        put("70a8a7365303d1c0f3eeb3c523500c577f25f1703b78ba93ed571a582ab3e9d8", new CountryBall("Spain"));
        put("048c54a35357082449bb3c9851a6896818658ca686bd8e15e82418a9d46778c7", new CountryBall("Mali"));
        put("cb38f7de41703f6d769cd2cb5099336c7fdd97c7225703d07f2b1adac2a72796", new CountryBall("Philippines"));
        put("99aeb3c9d5d6504095d91f3252072dec581dacf41928d1f4433884293436a7f7", new CountryBall("Union of South Africa", false));
        put("155cf1d85aac7a0a0c8f5a1411579c253fcfd738cffd106197081d1a73cf7e84", new CountryBall("Israel"));
        put("03062c4bb47fdb9a8272c3c1421e4b3e8cabf28f54b2b769c3e0155cb811ef18", new CountryBall("United Kingdom"));
    }};
}

enum Hint{
    ALL, COUNTRIES_ONLY, NON_COUNTRIES_ONLY
}

class CountryBall{
    public final String name;
    public final boolean isCountry;

    public CountryBall(String name, boolean isCountry){
        this.name = name;
        this.isCountry = isCountry;
    }
    public CountryBall(String name){
        this.name = name;
        this.isCountry = true;
    }

    @Override
    public String toString(){
        return "[" + name + ':' + isCountry + "]";
    }
}

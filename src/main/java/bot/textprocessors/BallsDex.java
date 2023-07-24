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
    private static final boolean resendImageURL = true;
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
            if(resendImageURL){
                actions.messageChannel(message.getChannel(), image.getUrl());
            }
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
            actions.messageChannel(message.getChannel(), "Non-country: " + country.name);
        }
    }

    private static final HashMap<String, CountryBall> sha256ToCountry = new HashMap<String, CountryBall>(){{
        put("ecf61045f6f9e5e895ac04dcfa64944e1f85816e4ef01054d975849070749a2b", new CountryBall("Libya"));
        put("7167d8fa31c8bd1ef2bdb1204073fecff01bd22ea058e75aa4875e2a722bc028", new CountryBall("Iceland"));
        put("8df30d3d95de4872d3a063edf275383e5437522d7649ce1b0cc15ed83149d4c8", new CountryBall("Macedon", false));
        put("ec8a15df7ccd722305562727818a933192da02e5899bf540a778add519735bdf", new CountryBall("Bahrain"));
        put("d62cfa0d1c947e45222347970d71060c75f908bdaf6caf46223fe4f5cd3721d9", new CountryBall("Turkey"));
        put("a38dcc81b98d816a398af102cf06fd31bcd28fbf95f3f20d383ca1a8dc104c2d", new CountryBall("Papua New Guinea"));
        put("1f23056b6b67eabd24550d28d632e3e8ea6b7a40367d82973509d15c3173b2f0", new CountryBall("South Yemen"));
        put("0e56386a101760890f221c4533839fa5c8165e6a4758f481185f43766457290d", new CountryBall("Mauritania"));
        put("9272705894e02ba001f7fea5e9e9e6e86d72e151d75272b882a6422f61de04c2", new CountryBall("Namibia"));
        put("07d84ebe975eb3413b47a05ebd6351569a24c0f0ed091fc39d4494232b73ebd9", new CountryBall("Romania"));
        put("5e72b6f691b60382bd8fff2ff8970268e4334400db394c74d1335e87c3bb445f", new CountryBall("Tunisia"));
        put("6c940da6466c56d7c27f6c07d113d68e32f2d9bb6f8c111d1062866c62a1e582", new CountryBall("Indonesia"));
        put("0eff76af22e4d5ca184f577c87d177e0f5d8838fb32c805acd521de205aafcc7", new CountryBall("Liechtenstein"));
        put("3600fa71ca80738e042ff839e7ddd8f0007bffc9cb79906d40402410683b54fc", new CountryBall("Central African Republic"));
        put("cfb9ac9017060ea7a1bf9c6b947977caec5dad4219585030bbfdef9fe96ebada", new CountryBall("Spanish Empire", false));
        put("631643c5930726ca867a94c7a1936dc425a34cb17863f71f003e6412b73cf6f5", new CountryBall("Ecuador"));
        put("84eb8a6746b0faf2f0dcc523f79cc0ab029cbb80b17cc80fd1c00422f0865e49", new CountryBall("Mongolia"));
        put("3750f286900f08d52cc29b26ae1e080ef6281cf5848c5036e2713cd387072465", new CountryBall("Fiji"));
        put("2971b2076e771402746bfe0bf118770355353b656ca60051547987c0bd2769a4", new CountryBall("Free France", false));
        put("d7638f3dcc615700af9ff2b47dd954a677aa42fe716af98be62d9473e953c722", new CountryBall("North Korea"));
        put("14e36e3322fa15b6663a0a750c3b88f3a6aca4968d30a6a60b6211f70a96effe", new CountryBall("Bhutan"));
        put("869bd129acbaeb7171b9424b4e35ecf9d26b4bf74932fb17917737fa3a4e33c9", new CountryBall("Arab League", false));
        put("d0e1a230f36da020bc90752313d8b1f3b3fa90e6224c84d6e91b09a5f5238d48", new CountryBall("Japan"));
        put("f45853571e6d71510d6a31aae49ec126a033bf6b113e589bea2822746042e315", new CountryBall("Kuwait"));
        put("67be5c003e6f68b4106b4fc19b48f7475d931d695eb42bc7c14f6b0fcf59bf42", new CountryBall("Prussia", false));
        put("f96c1545a9beee6a6e2a00845b4df37736bbfabc0863e4c44be659f0e9cb10cb", new CountryBall("Cameroon"));
        put("ba2691dc2a8a2421a3541697fff1112f299568ca98fe70878248cce5e4a5c3be", new CountryBall("Bangladesh"));
        put("e0a700a42e2fcefdff4ffb3fa872c20b8aab0bab11d7b9cff74fbafc5e8c7c9e", new CountryBall("Iran"));
        put("69531f2a342918a8f63c269ff0734eee4bb7f16a10d8eb5f8c891ecec8ae2a7e", new CountryBall("Somaliland", false));
        put("c1bd6221aba1134926d301fcd13b34f81cbd7a6d5774f79089d6ffd63eb5e969", new CountryBall("Norway"));
        put("6a246a30bf4976ff6242c3e16abd0ae36a94ed6368669169de8205727207ecf5", new CountryBall("Belarus"));
        put("4f87d9df8286d3b94b1c4298400099e13542ff83a1a22c30cf2595b236846692", new CountryBall("Slovenia"));
        put("779c7121be07ba31f1bc3d4a408abffb372d8cd1db877cd03a89810e647b1b3d", new CountryBall("French Indochina", false));
        put("cc5159ddf5a03140ab15207a5ecff2e0a7bd4976838b533caef289637e4ecaaf", new CountryBall("Khiva", false));
        put("7c9b1c8d0a499655f9b2ae62d4105e6385381946814d3434d2097340f352cf05", new CountryBall("Polish Underground State", false));
        put("cb660953318f6baaac3f924909443b1f8f4357a1b872bb9871046119ef067be1", new CountryBall("Hudson Bay Company", false));
        put("a9e6f36b9e792a318604e039b10cbfa45dab073f9df9b6282db1a5cc81419144", new CountryBall("Greece"));
        put("ab93a63035df0be83662bf38b86340a0fb05adf240f4a4cf78264bcfc73e4e5a", new CountryBall("Ghana"));
        put("bce767d7f6f6ca52aa77227914c0f04c8012ba17ed66da1ec0c01f62f93bb1a7", new CountryBall("Republican Spain", false));
        put("233cc963b58a7cad40c28256dc04194089d575a9f9ad0a4a2b7e28dc91a3e760", new CountryBall("Gambia"));
        put("4b89e051bac493929829b726f52df301134ac59707a5ddca4e1fabbe4591a007", new CountryBall("Somalia"));
        put("12ba841422a6801929131d4be9cc5f299a82ec439575c4a334cdc56a5b152ff2", new CountryBall("East Germany", false));
        put("b378dfce79b89956d279ab8fcd0671069375edbe2c3d191877636cddd0d8fd80", new CountryBall("Ancient Egypt", false));
        put("92e0347527666770e7894815ce28cd4a3c5ed969bd42c28ec1fc8d9166c4d25a", new CountryBall("Weimar Republic", false));
        put("ca5ba7c5cb0ff33616c606242184ed052bee6211b69b41fb2862b15f310dda9a", new CountryBall("Free City of Danzig", false));
        put("57bd2836936c87e62622bcfe7fefc8decf41e603123a796bfd25b7c5bd65da03", new CountryBall("Tanzania"));
        put("18e53b6d6611338a9a5553129ae6d039d421cdf632b5fa9007285fbe9ad1e758", new CountryBall("Vichy France", false));
        put("bdb222446ff8923270569a7ea15c4dbc0e8fa6fc725037973424c2fec58c0bdb", new CountryBall("Nicaragua"));
        put("46cff59fdeaae334de3bbe1ff43703a15ea7564392af8d8f68cee3b11f940fcd", new CountryBall("Ottoman Empire", false));
        put("0b3fc5f287f98f31f6d726d50eddfef94faa01b3c95fa7e0161863c950cfbccc", new CountryBall("Burkina Faso"));
        put("c15598d395006dba4ef6c4abbd56beaaadfe2dd75c031a59bb4d2e26fc08f311", new CountryBall("Haiti"));
        put("3698dda50da6a8844952cfd44645ee8c665610c24ed70f86f2dd4a460bf67bc1", new CountryBall("Kingdom of Egypt", false));
        put("bd3fa1da9d19bb8650a4901fce91c02e2053b5dd36298dbccdef1033ca85d2cf", new CountryBall("San Marino"));
        put("9e62d16fa1956859ccaf0bdb0adfb07854970af13dff366fa10a088e3ca704e6", new CountryBall("Mozambique"));
        put("52f2f3e6ee0e16d785f27fd98cc332c1633a553fd15b431a48a7b80991a189e1", new CountryBall("South Sudan"));
        put("d44c55828d4ab6726103ad59f3b7e90c169f75526bd363f77a0aa648ba0836b8", new CountryBall("Western Sahara", false));
        put("b95b58d4cb62de683ba8ce6c0ae79c9ee0e3668564f45729e2a0c57e560b42f4", new CountryBall("Xiongnu", false));
        put("22a5e0d1c41dcbf24c516a123ea78ad0d2b547f0fd69d8f69ac4869c08c415df", new CountryBall("Lithuania"));
        put("eee53157c6572801406224a66cc1023f8190a9ef3f85e648acb826479d47e403", new CountryBall("Luxembourg"));
        put("8f44694b48bfab88e3a547735fd57ddf996747e54a392852d994c06ea556fa30", new CountryBall("Paris Commune", false));
        put("c4d90ee918cac92b58297813c66dde2e2cddd91c07af2e531d3c7baf46828ab9", new CountryBall("DR Congo"));
        put("2c31b764d56f2d357361e91a68914186cd0f6a20c100f65bcc4e007c9b9c0ff0", new CountryBall("Montenegro"));
        put("ffbc36d46dfdc37fe743a7580cc8b5686cac228a7021e64ce8b5d6543a2ec360", new CountryBall("Congo Free State", false));
        put("19b6e64f409abcc30512370500c498f5cc67decc28d429bb17f5e98b56b1334e", new CountryBall("Mongolia"));
        put("c95717902878af48264253ad192f01a4b3d5644610573d91b9e0fdf32326dced", new CountryBall("Netherlands"));
        put("9a78593728754da0015e593b1672738f8798adfba787cedc9e5c56566a78b2b1", new CountryBall("Moldova"));
        put("36855cbe895a7ddd109e5c2e9be6fb1208ba09284c5c3c1e743a61850e9f8c0e", new CountryBall("Mexico"));
        put("2f17654bf0adf9ce0bbbd2f1e0fe56a0dd906d28acbee9cb844fde586860c6cc", new CountryBall("Panama"));
        put("1bf9f3d78c7bd61d876d8445a5cee939fec409ddd1038b0f1ab1732d38d3dfab", new CountryBall("Francoist Spain", false));
        put("6f2e2ac89dca2067f7d88bffd5d5db08fd12822c2c5606da8768f372c226385d", new CountryBall("Nepal"));
        put("d6e02a697ef2e8f909160a2d824477ae6c793abb42d700ce4555b9e376de5f95", new CountryBall("Egypt"));
        put("4e3192e4d312c225b091738b0c520ce0f4d2e03b75086715c6817c1a60c82807", new CountryBall("Niger"));
        put("55c34179c8f17a9de83cff7e37644ea21aefde8a8cecfcb85eb47e8d76f1d55c", new CountryBall("Nigeria"));
        put("37d165439e6a154bb8dbb3d3bbcd4809d2317076c0ec802901ad931b23819e45", new CountryBall("Guyana"));
        put("571e6966d05f15719e5111b417ac36e948f895410dd7b939ddba1b5dae8428d2", new CountryBall("Serbia"));
        put("801e6e34611cbb7fa1ce7cf3867eeafbf2b7fa5a5e7e0521dbd4b399b8df15dc", new CountryBall("Lebanon"));
        put("9ef8f0636fe31e81d44189a609ea4711aed7347098c31d22e65894fddb25a59d", new CountryBall("Liberia"));
        put("6aaf9e6965fab8a5adb7020c8f65b54ab5f605c19ca7f8ca52ca5474421ca5bf", new CountryBall("Portugal"));
        put("7921815af1cbee26deb0fcf931ea5b29ec1b7b2ebe2dfc44af61cdc23ac14f69", new CountryBall("Manchukuo", false));
        put("aa8843322e686c87bd289830354c102a400024d89c0bd8069b21411bc402c13a", new CountryBall("Bahamas"));
        put("8bb3ada62c1302f2dece2dbd543e4d7cf8ac1182196075d79d3d325e2dfac7e5", new CountryBall("Latvia"));
        put("3dbbe789461c2bf0ba4e35b453713d0621b8bb3c9745667b35668ab2f2ea891a", new CountryBall("Austria"));
        put("d3a364a58cfff6c8ce53be93eda9ab40ac954788de0071ee6a33c8ed67e96b93", new CountryBall("Kiribati"));
        put("98169d9faeb3763ae2acbf8cf5c3025a7d85ec7e487a5b1427f7988e97cb1762", new CountryBall("Byelorussian Soviet Socialist Republic", false));
        put("4a39f71085cf0bf301f333c3c423688db9ef6ed51b347238243676753937ca42", new CountryBall("Denmark"));
        put("ec4029269e8889313499e34ba2a377a40970e482578ab3d8e02dae7499e02d1b", new CountryBall("Australia"));
        put("a73fd6394328dc9fe8689a5ce2f5e42c67f289003fc8221ab52f006d15c328c7", new CountryBall("Finland"));
        put("5f5472701f86c49857cf1c643990ccc035cb701b2881323d6d63da8d571991b1", new CountryBall("Antarctica", false));
        put("d3f9c4eb68ef9bedd60ff0c8b2b2b2f97d8e4107bba79c35ed74425da06c7e85", new CountryBall("Chile"));
        put("c8dca3af0742e07d9adb383085bc0b4fba954a53378ed13707518c14ef3415c6", new CountryBall("Cuba"));
        put("88a73939311d49e81dec6955076b6a89f4e40781fcbcd02a32062080b4269fa2", new CountryBall("Babylon", false));
        put("d28cb9c03d6c652b5cf18f073f42ad5329f639763359c97a81dbecff438b3b4f", new CountryBall("Paraguay"));
        put("53471ab17239dbf185fcf4a407debbdb0dfd3be333dd590f67147d6531a5cfc1", new CountryBall("Slovakia"));
        put("87ddaa091ed96d6bdc2f31a003a9797484e3d4ca8074901081b29305288c9921", new CountryBall("Botswana"));
        put("3104b339ecdfac444a97bc6a65a15ad71d39e98aacaae407f681b5c4582fda66", new CountryBall("Portuguese Empire", false));
        put("d0544713e6c9042bc421577fecc1dce3d0e732926a3b04983847e89ff1a9b74c", new CountryBall("Ethiopia"));
        put("836cf19db7d35dd6c48df0c760b073e1806bd89f8bab43daa490d78133ff57e4", new CountryBall("Uganda"));
        put("397e9b41af9b0b51ec06d95899142af09d9bae5c8c814d77add4469f7551c702", new CountryBall("Chad"));
        put("fd631fdd1c2a02c2cbe86c5481e009f04efa486e973f7bb03ea77e74359dd422", new CountryBall("Bosnia and Herzegovina"));
        put("1644605f98b8b0f4aca48635b2e3925ad5d8f13146189e86c01a7e0b12623105", new CountryBall("Saudi Arabia"));
        put("6a93545efd749b18490c98bc3036d96173bc9614106782749a51cd700f3164dc", new CountryBall("Uruguay"));
        put("59df185bd154b77f8c57f8f4aa79f08866bb98cd7d2a79ec4d0b1091ba3f20b7", new CountryBall("Morocco"));
        put("1a1f463b708b7af594396eab10ec04715396630a3f93528c50f9e1369b5aed0b", new CountryBall("Kyrgyzstan"));
        put("834f25bed4a2365d904ce19108c3ed33d7b143bb819b381485fcb6b35322a72f", new CountryBall("Syria"));
        put("b9d2674b214cf45e5aaa3004cb9d33b19e21f808908340caf6d78ae64236fe11", new CountryBall("Madagascar"));
        put("064ecc992b5e5a261a3cdd1459063b19ff4f4f9d1df2e693783b947bff0da1ee", new CountryBall("Bulgaria"));
        put("871a38e5c1e42fbaabbef7145b432732180269189bd5ce313ee03613a0ab0653", new CountryBall("Brazil"));
        put("15c03e19e47b86fc1b17ee97058264c30504abf5839665f1f2b1843882f55677", new CountryBall("Peru"));
        put("1008fbbce7f3fb8ae49285b59283e5d0716d2c79031a8c3680665d87286db4e5", new CountryBall("Laos"));
        put("a3cf07e312e8c886190f56e260814cd17edc15fde1e7fb3cbd258d2b70f4b40f", new CountryBall("Tannu Tuva", false));
        put("6e1ec8efc0363324acaecc32559afd21a808c7316aaf6677128a1d3ceb988a5f", new CountryBall("Burundi"));
        put("c1b9f34c57f00912acd5db2da3f23ce08f2f627f052f4f7c40533a8fb705c7d4", new CountryBall("Seychelles"));
        put("54aa5a3f7decf92db48884e339a4f05de1b442a8c9911f8dd174cbb673fc6822", new CountryBall("NATO", false));
        put("2dab48501fd3005194943e3535c5c2a39d22ded1168e2bed59dd02601e30da3e", new CountryBall("Albania"));
        put("55f8518e604ac81179e6a18f87477adce7792aa686078ea6d9af30e422490889", new CountryBall("Honduras"));
        put("2c5084a70446ad3ed8eab00957fd60f88c977ae827e7d01840033ca6cb751269", new CountryBall("Zimbabwe"));
        put("2af725eb4cc17af0e41799912ce4b7620b70458ebba550ad733b07edf5731975", new CountryBall("Suriname"));
        put("cb466dbd7f6f92fecfbbf1d0caa743c291b5ec5c1e73cc81b29c005be5e016bc", new CountryBall("Qatar"));
        put("165352b4ebcc17ca871adafc2253b7338c6b85fb1fc1577c741adcc0609703dc", new CountryBall("British Empire", false));
        put("97d969075a04a639d47ae8ff98fa91cf489d8ee83a9bf127a81088af5f443241", new CountryBall("Afghanistan"));
        put("8eedb91e8d2b047be4da841066d365b5c7d2211770b068b9830aae9afb06ca3e", new CountryBall("Cyprus"));
        put("abab66eb4ae24f870d08cd6e5f3f85a5d06b72463dd121dd97e39bef6e24dc0e", new CountryBall("UAE"));
        put("1f105f87fc28a5993c0ef0a5d92261fa7fb1aae191149946d6b83e74e38d2d16", new CountryBall("Turkmenistan"));
        put("2606fbff46e88cf07686b1b4f1b3dd8703aab768ce94bd0e4681053f585091d6", new CountryBall("Tuvalu"));
        put("e5af428e97a5f5f9bb08c3438bb53135d34eee0cc3aa6ba8eda470a5b3c1dcb5", new CountryBall("New Zealand"));
        put("b3aaef8197a056db6057f2d84b349f88d364c8c5b7fb812408fc9c9b0bff2d38", new CountryBall("South Vietnam", false));
        put("02fbda025fada36f60b2074dbe7c229e44212457190ca14da0ea81801b4592e7", new CountryBall("Uzbekistan"));
        put("d734b05b6c70b73737d6a58a7df5e0fb7a4be6b7975a68d042a1ced241bdd5c0", new CountryBall("Maldives"));
        put("d9b314289263f14ef5e37007ef908ffd5a2acab098ea9e0582aed61a48ef51ef", new CountryBall("Taiwan"));
        put("dd852b061e118bff61d649cb0b83ce3d30e1afb03808c50f2536df92e2e29a2f", new CountryBall("Armenia"));
        put("969d7787da0713a4fb5cbaebe116fc2c0fce9ca7b6a7fd604d649baa06d2821c", new CountryBall("Ceylon"));
        put("138c8ba527438e774e67fd24bdd99759393528cda01b5b4cdfe58a16c985a5a5", new CountryBall("Ancient Sparta", false));
        put("de55f6b3c6d1a94ce1fd55a8e1a41708f2c3d7a4b62e6b8f04c2b53abf68926e", new CountryBall("Gabon"));
        put("ee49a6f5ca05b0ac774519eab2f674e90614a836d303726352a72bdfc670a94f", new CountryBall("Ivory Coast"));
        put("8886c8491cf7e514a5fa82a83376f316c5b17823938ac4e13158d64ac9c84ed0", new CountryBall("North Macedonia"));
        put("84bb93cca54bfae77c8d746032768967e4d1227b0f2d041a8aa471d816da07c9", new CountryBall("Sierra Leone"));
        put("17106fe76f53c550905a0f99e42594626c8a17d4eea9991d3e2d9b741fbbb29e", new CountryBall("Angola"));
        put("a83ef7604a59ce5214417bfc14432c38f8eff6215cd69f94ddb0f8aadbb4e402", new CountryBall("Yemen"));
        put("6ad219694eae8a5863f61e4d4c1678b08b598bc18281b3cb97801315a57df936", new CountryBall("Djibouti"));
        put("6ed351a515f7781a485fdf3df11d36fe978a7418f1d03a20eb0cc14b51eeca77", new CountryBall("African Union", false));
        put("3fd96f8b946153d085a45507cee97be500f6c5f2ad95e88830738db859ffbf94", new CountryBall("Palestine"));
        put("0b86ed4cc36d75914255d1465f83fb9dc1fc4eae341e6a7d8cfb971b56d0f54e", new CountryBall("Malta"));
        put("d79fd0a4755a386deea3522aecfd714b5e25694ff8cf7fa093750b25625244da", new CountryBall("West Germany", false));
        put("8fb36573aa3ace1c040516fa351958ed32b462498ee46000f3492cd01df3ff6e", new CountryBall("Singapore"));
        put("854bd45538c84ae746eade5688f74c788807ba3ccb1af2549165b97af505cee0", new CountryBall("Czechia"));
        put("36f227382def9a4efed1581e9f99540425b6a6f66c0408000ee6d294c837a63a", new CountryBall("Georgia"));
        put("72f90956af463687ef45a534ac008f3f98fee3bca40c25f2c4e168e5cbc8d210", new CountryBall("Croatia"));
        put("e370caf9aa27910a404984f770b9503ccca754d2194fc38e8878199a3cef30de", new CountryBall("Hejaz", false));
        put("20ecf50c86f6456acdbd1d04c61ce602ab8c5ab86e18116580e42fc1293a6d92", new CountryBall("Pakistan"));
        put("21fffdb9e1a3d41da4d0ee6f7f8a127b259114ccfba8901ae9fa122ddb12edd6", new CountryBall("Zhou", false));
        put("b9bb6d6ac9dc9b4f49241a7c826f3cfdd6fdcaf55e4a43cc8fafa21c11fde4e3", new CountryBall("Azerbaijan"));
        put("077e8855fbb02f62a83e6402ca183cea957f1ad7b42faab12e0c3449502923ba", new CountryBall("Napoleonic France", false));
        put("ef3171701684a157839450ca44e9ed4d04fe52ac638dc2ad3c0609dcaad36b1d", new CountryBall("Kosovo"));
        put("b11656549f5bc3e2f2c30dba96bc7d5abe2cff43ac7e9779d73ed96e77f30f00", new CountryBall("Kingdom of Greece", false));
        put("d9e98f5ef8542536523d0a48b7161d5b5e8344992f4b0c27b67431c545a2e7e2", new CountryBall("Belgium"));
        put("bdae2452e8dbb903e2fdc6f18c5772ac43465af6a5b8ee71dca85d72f6ace543", new CountryBall("Eritrea"));
        put("011f7153aab2367dee08f57f555831be4e1746383b0380dfdb84d8092cc74cf6", new CountryBall("Congo"));
        put("8e38b8b4d1b409edd13c84dd796a83862f8049b2fd4f68514d5bcc67ceaedeaa", new CountryBall("European Union", false));
        put("3ae7e42c0b4eb695c7cbf3319172421cacfb853bd7af08355e5b4248cc23b171", new CountryBall("Ancient Athens", false));
        put("f0b54d0af2b336492714e2dc315e2ba40ff59bf326e29a43506afa9209248607", new CountryBall("Hungary"));
        put("37fe191bbfc4165931dd3539aebb8c9208873bce4eab41211d02e11e735aa6d1", new CountryBall("Yugoslavia"));
        put("86a2c6917521963bbf3b8fdbf369649f8542430356c6db4ae6117584aed0c34a", new CountryBall("Sri Lanka"));
        put("6b2bcc7fa79450d839067bb56ff8c2c93929211e28ae928a6e7d1f8c75c6d96a", new CountryBall("Germany"));
        put("29e96a035304e47bc1d5aa9d5b2810b0a8a4d447b76b1ff3a14fa49cf29fbd9a", new CountryBall("Bolivia"));
        put("83f37c900411d872ba7c95a42224b826615ae33935077d1973947513f5df7175", new CountryBall("Cambodia"));
        put("1d276f7de47411692bb92fce8f760a89bd2478f65f52d37805dbee8bbd0baeb4", new CountryBall("El Salvador"));
        put("82dd401f26d506144fa3d3b62503cf54958954184d3174820924cbda20946ef1", new CountryBall("South Korea"));
        put("90b0437bc29fc06c8bb113cde73f9d0d609115e20b0017aadbb37faf10f9d780", new CountryBall("Gaul", false));
        put("a149ac9df2845b18a1266f869cb8fe8fbbf14f274292ad9b8f7e924311e2884d", new CountryBall("Switzerland"));
        put("c2e957b66614a9e5404462b33b7bfa00401f6669eb2b74e01807f0ccc360e318", new CountryBall("Senegal"));
        put("cb559481377add80ef2f488e26f0485def6399b4b015f5a1f4e5f5d759589ae7", new CountryBall("Germania", false));
        put("ea1dd8eb429c6b034ea99353b78795346e746e8c2ed63eaa5d033afcd0b7fe41", new CountryBall("League of Nations", false));
        put("8dc103d431dd8c6995e1001939129278ca1f4897226f2b29d161c14a6ca93c4b", new CountryBall("Thailand"));
        put("4f9e7401b29475da42124f4145f07ef93b12f043e72aad088736c8e313afb7bc", new CountryBall("Jordan"));
        put("a8adde8244a81e6a09c1ef77052ef32538ae217e464f931fc87eb37af98666bf", new CountryBall("Zambia"));
        put("165fe94087ca8b6440f41c58b714940875680b781c74abae340d230d495f6be4", new CountryBall("Argentina"));
        put("e6ac0468e8073f72463a5dc800da8db1dfdf21fdf07d7aa8dafe7324f39f9e7c", new CountryBall("Oman"));
        put("8ab075a0c4c400adf6599f0ba250d9f5f23f50d44ae091def9bca8e1930d3cef", new CountryBall("Sweden"));
        put("c6514c758d675e3b228c73b986daf6c40507be111207c076bf11099cbb5677ae", new CountryBall("Mayan Empire", false));
        put("6c1698066646250d67785f92ee7a60609b17cbe90c94e1d9fcf0d5afb01a56e4", new CountryBall("Iraq"));
        put("a4dc33088f9d09266ea34f7becc8a68712771dd7472bde89e58d197885003e7f", new CountryBall("Venezuela"));
        put("d8501e6ee7656641b9664a92e301bd8eb42fd9168e8529836130731a8b0abf91", new CountryBall("Guatemala"));
        put("d5495fcecd3e769df60040ab8db76eba23561f0d8b65139bafe5228a65896f47", new CountryBall("Estonia"));
        put("fb31465cfdcb48846820836ef62ef543b7b3607fd9f53543116f97b4f40ba9d1", new CountryBall("Spain"));
        put("1e49f3bdba95c3c6aa5006859a5236144bf76da2f870284aa9951032c8f8d638", new CountryBall("Mali"));
        put("26871c2e15e184a8baa6f96e05d8b240d100bf6aa064229a3a834e44858e51d9", new CountryBall("Philippines"));
        put("cd093dec8400479f9b7dee81e02e1b96e42fe8f263293142f1982ea8d2754bae", new CountryBall("Union of South Africa", false));
        put("291f1add75b580f027ac75ac3d98e304f06a78cf8e4d4fb9f64e29a3fd380e49", new CountryBall("Israel"));
        put("6bc3a50b8cb47706182543e6133b66a7ecc22523aff43437eea0800ed66b85db", new CountryBall("United Kingdom"));
        put("ee5ab56e1ced755dbae2cf62d03c38f5e2bda1e20c4f635b84b1e769a6a0249d", new CountryBall("Italy"));
        put("4c7bcb54a23fb96987e37f8214070958bdf97249d94ad53744c22218bc6f57a5", new CountryBall("United Arab Republic", false));
        put("0394bbbde5880dbf412dfa286ae65e40c7d401da0930db4f7aaae0e0ae24cb2d", new CountryBall("Ukrainian Soviet Socialist Republic", false));
        put("74b923b73e3a19573c63f56dcc3ae2424f601c7be37b23bfc56424854dae6262", new CountryBall("Kingdom of Italy", false));
        put("335666cb10f6f258eb1b8528f3c6dd03298adf7f3bb3cb8734275a740a90c10e", new CountryBall("Ethiopian Empire", false));
        put("b0ef78b6a74ff01a5947789abbe903f4b6df98c5328d471d3e732e60cc4d2207", new CountryBall("Japanese Empire", false));
        put("d1e0024880adb40021efc76aeab1a5d9bf425d3cf93d599093140920b429453e", new CountryBall("Warsaw Pact", false));
        put("9fd05c0da75dc507fa7cb4781b343db581057721a23d68a21bf791531880faef", new CountryBall("Tibet", false));
        put("f50f51a585839097d197fd73dc58eb5ade0864229c3df5262bcb28855789aeb3", new CountryBall("Czechoslovakia"));
        put("21f88ee8db9e591556d19f9f97ed04a84ca16c0e70a90a78a0b7b2d9c8fbf903", new CountryBall("Republic of China", false));
        put("50ef2d29b27663fde1fc3d2349c3e66c255260d6034395050dff6ccd03e15fb9", new CountryBall("France"));
        put("36fe7c8f17b307046c91bf2d053eb90fca73bbaae5751c1721809abe1c823426", new CountryBall("Scandinavia", false));
        put("e31a1826d05a322dbc717f4384da121235c636298932a8364ee019a4e68213a9", new CountryBall("Vietnam"));
        put("3f7770164e7add2866508fb011b1e53a84f44ab8afd68941688df524d23a1589", new CountryBall("Vietnam"));
        put("eca44d4d64f085fad113c72ae5d7404419eafc37b8a4e1c399c85a302707c1e6", new CountryBall("Reichtangle", false));
        put("8c2c079f510e3333a50abcacfc9c9813a6ac3ef667999e49de10976efbbf9f91", new CountryBall("Roman Empire", false));
        put("0d9a4def950a12c61cff7efc10152bb969d074cd4a084969b7536e127ff558d8", new CountryBall("Soviet Union"));
        put("b4a9299a607e9dcf0484b7e75da4e1deb3a2860d111288f5e293264061c70a40", new CountryBall("Ancient Greece", false));
        put("9e30ebc19914950c38336fdd9e9cca13ed8b8cac901dfc2e1ba86c54e990b287", new CountryBall("Franks", false));
        put("552524ce5d2c6a218f75904862d4a892040efbfafab809c99d518ca8506419ec", new CountryBall("Poland"));
        put("75bdcbfacf30653e44b1c527362ff3883325b5560bb6ed7f0c821196318e4eff", new CountryBall("Kazakhstan"));
        put("546930fb28a81632ec623c1a470bef10282f898821f3724c664f3e27114ff735", new CountryBall("Byzantium", false));
        put("c8f88321883df007cfa282614b21900327ce58663deab390cdd5acb0d90c543a", new CountryBall("Ireland"));
        put("e02120c91bdd80a5814a0aa89fc4fbbe7206735f71b317a41ed5d220faa1e6cb", new CountryBall("USA"));
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

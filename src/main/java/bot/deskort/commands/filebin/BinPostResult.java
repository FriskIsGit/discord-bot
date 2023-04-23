package bot.deskort.commands.filebin;

import bot.utilities.Option;
import bot.utilities.requests.SimpleResponse;

//if response is present and code is 200 or 201 - display url
//if response is present but code is other than 200 and 201 - display response code and error message
//if response is not present - display no response
public class BinPostResult{
    public String url;
    public Option<SimpleResponse> responseOption;

    public BinPostResult(String url, Option<SimpleResponse> responseOption){
        this.url = url;
        this.responseOption = responseOption;
    }
}

package youtube_lib.cipher;

import java.util.List;
import java.util.Map;

public class DefaultCipher{

    private final Map<String, CipherFunction> functionsMap;
    private final List<JsFunction> functions;

    public DefaultCipher(List<JsFunction> transformFunctions, Map<String, CipherFunction> transformFunctionsMap) {
        this.functionsMap = transformFunctionsMap;
        this.functions = transformFunctions;
    }

    public String getSignature(String cipheredSignature) {
        char[] signature = cipheredSignature.toCharArray();
        for (JsFunction jsFunction : functions) {
            signature = functionsMap.get(jsFunction.getName()).apply(signature, jsFunction.getArgument());
        }
        return String.valueOf(signature);
    }

}

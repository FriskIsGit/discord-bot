package bot.utilities.requests;

import java.util.ArrayList;
import java.util.List;

public class Cookies {
    private static final boolean SPACE_AFTER_SEMICOLON = true;
    private final List<Cookie> pairs;

    private Cookies() {
        this.pairs = new ArrayList<>();
    }

    private Cookies(List<Cookie> cookies) {
        this.pairs = cookies;
    }

    public static Cookies New() {
        return new Cookies();
    }

    public static Cookies parseCookies(String cookies) {
        char[] arr = cookies.toCharArray();
        Parse state = Parse.NAME;
        StringBuilder name = new StringBuilder();
        StringBuilder val = new StringBuilder();
        Cookies biscuits = new Cookies();
        for (int i = 0; i < arr.length; i++) {
            switch (arr[i]) {
                default:
                    if (state == Parse.NAME) {
                        name.append(arr[i]);
                    } else {
                        val.append(arr[i]);
                    }
                    if (i == arr.length - 1) {
                        Cookie cookie = new Cookie(name.toString(), val.toString());
                        biscuits.pairs.add(cookie);
                        return biscuits;
                    }
                    break;
                case '=':
                    if (state == Parse.NAME) {
                        state = Parse.VALUE;
                    } else {
                        val.append('=');
                    }
                    break;
                case ';':
                    state = Parse.NAME;
                    if (i + 1 < arr.length && arr[i + 1] == ' ') {
                        i++;
                    }
                    if (val.length() == 0) {
                        biscuits.pairs.add(new Cookie(name.toString()));
                        name.setLength(0);
                        break;
                    }
                    Cookie cookie = new Cookie(name.toString(), val.toString());
                    biscuits.pairs.add(cookie);
                    name.setLength(0);
                    val.setLength(0);
                    break;
            }
        }
        return biscuits;
    }

    public Cookies addCookie(Cookie cookie) {
        pairs.add(cookie);
        return this;
    }

    public Cookies addPair(String name, String val) {
        pairs.add(new Cookie(name, val));
        return this;
    }

    public Cookies addAttribute(String attribute) {
        pairs.add(new Cookie(attribute));
        return this;
    }

    public Cookies copy() {
        List<Cookie> cookies = new ArrayList<>(pairs.size());
        for (Cookie cookie : pairs) {
            if (cookie.hasValue) {
                cookies.add(new Cookie(cookie.name, cookie.value));
            } else {
                cookies.add(new Cookie(cookie.name));
            }
        }
        return new Cookies(cookies);
    }

    //case-sensitive name
    public Cookie getCookie(String name) {
        for (Cookie cookie : pairs) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    public int size() {
        return pairs.size();
    }

    public String asString() {
        StringBuilder str = new StringBuilder();
        int len = pairs.size();
        for (int i = 0; i < len; i++) {
            Cookie cookie = pairs.get(i);
            if (cookie.hasValue) {
                str.append(cookie.getName()).append('=').append(cookie.getValue());
            } else {
                str.append(cookie.getName());
            }
            if (i != len - 1) {
                str.append(';');
                if (SPACE_AFTER_SEMICOLON) {
                    str.append(' ');
                }
            }
        }
        return str.toString();
    }

    private enum Parse {
        NAME, VALUE
    }

    public static class Cookie {
        private final String name;
        private String value;
        public final boolean hasValue;

        public Cookie(String name, String val) {
            this.name = name;
            this.value = val;
            hasValue = true;
        }

        public Cookie(String attribute) {
            name = attribute;
            hasValue = false;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}



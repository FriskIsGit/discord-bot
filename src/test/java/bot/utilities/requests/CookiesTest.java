package bot.utilities.requests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CookiesTest{
    @Test
    public void bigCookieSize(){
        String cookie = "__cf_bm=WckMJxjgdoJqxStdmcq9FbuQ=; path=/; expires=Wed, 21-Jun-23 01:10:46 GMT; domain=.nokia.com; HttpOnly; Secure; SameSite=None";
        Cookies biscuits = Cookies.parseCookies(cookie);
        assertEquals(7, biscuits.size());
    }
    @Test
    public void bigCookieFirstKey(){
        String cookie = "__cf_bm=WckMJxjgdoJqxStdmcq9FbuQ=; path=/; expires=Wed, 21-Jun-23 01:10:46 GMT; domain=.nokia.com; HttpOnly; Secure; SameSite=None";
        Cookies biscuits = Cookies.parseCookies(cookie);
        assertNotNull(biscuits.getCookie("__cf_bm"));
    }
    @Test
    public void bigCookieMoreKeys(){
        String cookie = "__cf_bm=WckMJxjgdoJqxStdmcq9FbuQ=; path=/; expires=Wed, 21-Jun-23 01:10:46 GMT; domain=.nokia.com; HttpOnly; Secure; SameSite=None";
        Cookies biscuits = Cookies.parseCookies(cookie);
        assertNotNull(biscuits.getCookie("__cf_bm"));
        assertNotNull(biscuits.getCookie("path"));
        assertNotNull(biscuits.getCookie("expires"));
    }
    @Test
    public void bigCookieAllKeys(){
        String cookie = "__cf_bm=WckMJxjgdoJqxStdmcq9FbuQ=; path=/; expires=Wed, 21-Jun-23 01:10:46 GMT; domain=.nokia.com; HttpOnly; Secure; SameSite=None";
        Cookies biscuits = Cookies.parseCookies(cookie);
        assertNotNull(biscuits.getCookie("__cf_bm"));
        assertNotNull(biscuits.getCookie("path"));
        assertNotNull(biscuits.getCookie("expires"));
        assertNotNull(biscuits.getCookie("domain"));
        assertNotNull(biscuits.getCookie("HttpOnly"));
        assertNotNull(biscuits.getCookie("Secure"));
        assertNotNull(biscuits.getCookie("SameSite"));
    }
    @Test
    public void bigCookieValueCheck(){
        String cookie = "__cf_bm=WckMJxjgdoJqxStdmcq9FbuQ=; path=/; expires=Wed, 21-Jun-23 01:10:46 GMT; domain=.nokia.com; HttpOnly; Secure; SameSite=None";
        Cookies biscuits = Cookies.parseCookies(cookie);
        Cookies.Cookie cook = biscuits.getCookie("expires");
        assertNotNull(cook);
        assertEquals("Wed, 21-Jun-23 01:10:46 GMT", cook.getValue());
    }

    @Test
    public void checkFirstValue(){
        String cookie = "__cf_bm=WckMJxjgdoJqxStdmcq9FbuQ=; path=/; expires=Wed, 21-Jun-23 01:10:46 GMT; domain=.nokia.com; HttpOnly; Secure; SameSite=None";
        Cookies biscuits = Cookies.parseCookies(cookie);
        Cookies.Cookie cook = biscuits.getCookie("__cf_bm");
        assertNotNull(cook);
        assertEquals("WckMJxjgdoJqxStdmcq9FbuQ=", cook.getValue());
    }

    @Test
    public void backToOriginal(){
        String original = "__cf_bm=WckMJxjgdoJqxStdmcq9FbuQ=; path=/; expires=Wed, 21-Jun-23 01:10:46 GMT; domain=.nokia.com; HttpOnly; Secure; SameSite=None";
        Cookies biscuits = Cookies.parseCookies(original);
        String processed = biscuits.asString();
        assertEquals(original, processed);
    }
}

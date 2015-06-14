package com.futurice.cascade.util;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.futurice.cascade.AsyncAndroidTestCase;
import com.futurice.cascade.i.functional.IAltFuture;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.spdy.Header;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

import static com.futurice.cascade.Async.WORKER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the NetUtil class
 *
 * Created by phou on 6/2/2015.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class NetUtilTest extends AsyncAndroidTestCase {

    public NetUtilTest() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();

        setDefaultTimeoutMillis(15000); // Give real net traffic enough time to complete
    }

    @Test
    public void testGet() throws Exception {
        assertThat(getNetUtil().get("http://httpbin.org/").body().bytes().length).isGreaterThan(100);
    }

    @Test
    public void testGetCustomHeaders() throws Exception {
        Collection<Header> headers = new ArrayList<>();
        Header header = new Header("Cookie", "Value");
        headers.add(header);
        assertThat(getNetUtil().get("http://httpbin.org/headers", headers).body().string()).contains("Value");
    }

    public void testIGettableGet() throws Exception {
        assertThat(getNetUtil().get(() -> "http://httpbin.org/").body().bytes().length).isGreaterThan(100);
    }

    @Test
    public void testGetAsync() throws Exception {
        IAltFuture<?, Response> iaf = getNetUtil().getAsync("http://httpbin.org/get")
                .fork();
        assertThat(awaitDone(iaf).isSuccessful()).isTrue();
    }

    @Test
    public void testGetAsyncChained() throws Exception {
        IAltFuture<?, Response> iaf = WORKER
                .from("http://httpbin.org/get")
                .then(getNetUtil().getAsync())
                .fork();
        assertThat(awaitDone(iaf).isSuccessful()).isTrue();
    }

    public void testGetAsyncChainedWithHeaders() throws Exception {
        Collection<Header> headers = new ArrayList<>();
        Header header = new Header("Cookie", "AnotherValue");
        headers.add(header);
        IAltFuture<?, Response> iaf = WORKER
                .from("http://httpbin.org/headers")
                .then(getNetUtil().getAsync(headers))
                .fork();
        assertThat(awaitDone(iaf).body().string()).contains("AnotherValue");
    }

    public void testPut() throws Exception {
        assertThat(getNetUtil().put(
                "http://httpbin.org/put",
                RequestBody.create(MediaType.parse("Text/plain"), "SomeText")
        ).body().string()).contains("SomeText");
    }

    public void testPutWithHeaders() throws Exception {
        Collection<Header> headers = new ArrayList<>();
        Header header = new Header("Cookie", "Bacon");
        headers.add(header);
        assertThat(getNetUtil().put(
                "http://httpbin.org/put",
                headers,
                RequestBody.create(MediaType.parse("Text/plain"), "Eggs")
        ).body().string()).contains("Eggs");
    }

    public void testPutAsync() throws Exception {

    }

    public void testPutAsync1() throws Exception {

    }

    public void testPutAsync2() throws Exception {

    }

    public void testPutAsync3() throws Exception {

    }

    public void testPutAsync4() throws Exception {

    }

    public void testPutAsync5() throws Exception {

    }

    public void testPost() throws Exception {

    }

    public void testPostAsync() throws Exception {

    }

    public void testPostAsync1() throws Exception {

    }

    public void testPostAsync2() throws Exception {

    }

    public void testPost1() throws Exception {

    }

    public void testPostAsync3() throws Exception {

    }

    public void testPostAsync4() throws Exception {

    }

    public void testPostAsync5() throws Exception {

    }

    public void testPost2() throws Exception {

    }

    public void testDeleteAsync() throws Exception {

    }

    public void testDeleteAsync1() throws Exception {

    }

    public void testDelete() throws Exception {

    }

    public void testDeleteAsync2() throws Exception {

    }

    public void testDeleteAsync3() throws Exception {

    }

    public void testDelete1() throws Exception {

    }

    public void testGetMaxNumberOfNetConnections() throws Exception {

    }

    public void testIsWifi() throws Exception {

    }

    public void testGetNetworkType() throws Exception {

    }
}
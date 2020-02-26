package com.geekbang.horde.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geekbang.horde.dto.Page;
import com.geekbang.horde.dto.PostData;
import com.geekbang.horde.entity.Post;
import com.geekbang.horde.entity.PostLikeUser;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.beetl.sql.core.*;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.ext.DebugInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @author tankdev
 * @since 2019-08-25 10:27
 */
public class GeekTimeHttpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeekTimeHttpUtil.class);
    private static CloseableHttpClient client;
    private static BasicCookieStore cookieStore;

    public static CloseableHttpClient getHttpClient() {
        if (client == null) {
            client = makeDefaultClient();
        }
        return client;
    }

    public static void depose() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
        cookieStore = null;
        client = null;
    }


    private static CloseableHttpClient makeDefaultClient() {


        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);

        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
        }

        return HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
    }

    private static void addHeader(HttpRequestBase request) {
        String headers = "原样复制header信息，注意删除Content-Length请求头";
        String[] headerArr = headers.split("\n");
        for (String header : headerArr) {
            String[] ha = header.split(":");
            request.setHeader(ha[0].trim(), ha[1].trim());
        }
    }


    public static PostData getPosts(Long index) throws IOException {
        String uri = "https://horde.geekbang.org/serv/v1/channel/posts";
        HttpPost post = new HttpPost(uri);
        PostData postData = new PostData();
        addHeader(post);
        List<NameValuePair> params = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("index", index);
        map.put("channel_id", 19);
        //设置发送的数据
        String json = JSON.toJSONString(map);
        StringEntity requestEntity = new StringEntity(json, "utf-8");
        requestEntity.setContentEncoding("UTF-8");
        post.setHeader("Content-type", "application/json");
        post.setEntity(requestEntity);


        CloseableHttpClient client = getHttpClient();
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = client.execute(post);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpResponse.getEntity();
            String html = EntityUtils.toString(entity);
            System.out.println(html);

            JSONObject jsonObject = JSON.parseObject(html);
            Boolean more = jsonObject.getJSONObject("data").getJSONObject("page").getBoolean("more");
            Long next_index = jsonObject.getJSONObject("data").getJSONObject("page").getLong("next_index");
            Page page = new Page();
            page.setMore(more);
            page.setNext_index(next_index);
            postData.setPage(page);
            JSONArray postsjsonArray = jsonObject.getJSONObject("data").getJSONObject("detail").getJSONArray("post");
            int size = postsjsonArray.size();
            System.out.println("获取post 列表 " + index);
            List<Post> postList = new ArrayList<>();
            postData.setPostList(postList);
            for (int i = 0; i < size; i++) {
                JSONObject o = postsjsonArray.getJSONObject(i);
                String id = o.getString("id");
                Long userId = o.getLong("user_id");
                Integer like_count = o.getInteger("like_count");
                Long create_time = o.getLong("create_time");

                Post postEntity = new Post();
                postEntity.setId(id);
                postEntity.setUserId(userId);
                postEntity.setLikeCount(like_count);
                postEntity.setCreateTime(new Date(create_time));
                postList.add(postEntity);

                System.out.println("获取 " + id + " 点赞信息");
                JSONArray like_user_list = o.getJSONArray("like_user_list");
                List<PostLikeUser> postLikeUserList = new ArrayList<>();
                postEntity.setPostLikeUserList(postLikeUserList);
                if (like_count > like_user_list.size()) {
                    // send post
                    List<PostLikeUser> likeUserList = likesList(id, 1);
                    postLikeUserList.addAll(likeUserList);
                } else {
                    for (int j = 0; j < like_user_list.size(); j++) {
                        JSONObject likeUserObject = like_user_list.getJSONObject(j);
                        Long like_user_id = likeUserObject.getLong("user_id");
                        String ucode = likeUserObject.getString("ucode");
                        PostLikeUser postLikeUser = new PostLikeUser();
                        postLikeUser.setPostId(id);
                        postLikeUser.setUserId(like_user_id);
                        postLikeUser.setUcode(ucode);
                        postLikeUserList.add(postLikeUser);
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpResponse.close();
        }
        return postData;
    }

    public static Page LikesAdd(String post_id) throws IOException {
        String uri = "https://horde.geekbang.org/serv/v1/likes/add";
        HttpPost post = new HttpPost(uri);

        addHeader(post);
        Map<String, Object> map = new HashMap<>();
        map.put("post_id", post_id);
        map.put("type", 1);
        //设置发送的数据
        String json = JSON.toJSONString(map);
        StringEntity requestEntity = new StringEntity(json, "utf-8");
        requestEntity.setContentEncoding("UTF-8");
        post.setHeader("Content-type", "application/json");
        post.setEntity(requestEntity);


        CloseableHttpClient client = getHttpClient();
        CloseableHttpResponse httpResponse = null;
        Page page = new Page();
        try {
            httpResponse = client.execute(post);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpResponse.getEntity();
            String html = EntityUtils.toString(entity);
            JSONObject jsonObject = JSON.parseObject(html);
            Boolean more = jsonObject.getJSONObject("data").getBoolean("is_success");
            System.out.println(html);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpResponse.close();
        }
        return page;
    }

    public static List<PostLikeUser> likesList(String post_id, Integer index) throws IOException {
        List<PostLikeUser> postLikeUserList = new ArrayList<>();
        String uri = "https://horde.geekbang.org/serv/v1/likes/list";
        HttpPost post = new HttpPost(uri);

        addHeader(post);
        Map<String, Object> map = new HashMap<>();
        map.put("post_id", post_id);
        map.put("index", index);
        map.put("page_type", 1);
        map.put("size", 20);
        //设置发送的数据
        String json = JSON.toJSONString(map);
        StringEntity requestEntity = new StringEntity(json, "utf-8");
        requestEntity.setContentEncoding("UTF-8");
        post.setHeader("Content-type", "application/json");
        post.setEntity(requestEntity);


        CloseableHttpClient client = getHttpClient();
        CloseableHttpResponse httpResponse = null;
        Page page = new Page();
        try {
            httpResponse = client.execute(post);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpResponse.getEntity();
            String html = EntityUtils.toString(entity);
            System.out.println(html);
            JSONObject jsonObject = JSON.parseObject(html);

            Boolean more = jsonObject.getJSONObject("data").getJSONObject("page").getBoolean("more");
            Long next_index = jsonObject.getJSONObject("data").getJSONObject("page").getLong("next_index");
            Integer current = jsonObject.getJSONObject("data").getJSONObject("page").getInteger("current");

            JSONArray listJsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
            int size = listJsonArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject object = listJsonArray.getJSONObject(i);
                String ucode = object.getString("ucode");
                Long user_id = object.getLong("user_id");
                PostLikeUser postLikeUser = new PostLikeUser();
                postLikeUser.setUcode(ucode);
                postLikeUser.setUserId(user_id);
                postLikeUser.setPostId(post_id);
                postLikeUserList.add(postLikeUser);
            }
            if (more) {
                Thread.sleep(2000);
                List<PostLikeUser> likeUsers = likesList(post_id, current + 1);
                postLikeUserList.addAll(likeUsers);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            httpResponse.close();
        }
        return postLikeUserList;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ConnectionSource source = ConnectionSourceHelper.getSimple("com.mysql.cj.jdbc.Driver"
                , "jdbc:mysql://localhost:3306/horde?serverTimezone=UTC"
                , "root"
                , "root");
        DBStyle mysql = new MySqlStyle();
        SQLLoader loader = new ClasspathLoader("/sql");
        UnderlinedNameConversion nc = new UnderlinedNameConversion();
        SQLManager sqlManager = new SQLManager(mysql, loader, source, nc, new Interceptor[]{new DebugInterceptor()});
        boolean more = true;
        Long next_index = 0L;
        while (more) {
            PostData postData = getPosts(next_index);
            next_index = postData.getPage().getNext_index();
            more = postData.getPage().getMore();
            List<Post> postList = postData.getPostList();
            System.out.println(postList);
            for (Post post : postList) {
                sqlManager.insert(post);
                sqlManager.insertBatch(PostLikeUser.class, post.getPostLikeUserList());
            }
            Thread.sleep(2000);
        }
    }
}

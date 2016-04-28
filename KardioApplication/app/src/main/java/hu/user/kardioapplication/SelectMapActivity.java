package hu.user.kardioapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by User on 2016.04.27..
 */
public class SelectMapActivity extends AppCompatActivity
{
    private List<Route> routes;
    public String[] saved;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectmap);

        routes = new ArrayList<>();


        saved = new String[0];
        try
        {
            saved = getAssets().list("gpx");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Bundle b = new Bundle();
        b.putStringArray("saved", saved);
        Intent intent = new Intent(this, RoutesAdapter.class);
        intent.putExtras(b);

        Log.i("file path is :", Arrays.toString(saved));

        for (int i = 0; i < saved.length; i++)
        {
            Route temp = openGpxToRead(saved[i]);
            routes.add(temp);
        }
        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        RoutesAdapter adapter = new RoutesAdapter(routes);
        rv.setAdapter(adapter);
        }

    public Route openGpxToRead(String filename)
    {
        String name = "";
        String desc = "";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream fileInputStream = getAssets().open(filename);
            Document document = documentBuilder.parse(fileInputStream);
            Element elementRoot = document.getDocumentElement();

            NodeList nodelist_name = elementRoot.getElementsByTagName("name");

            Node nName = nodelist_name.item(0);
            name = nName.getFirstChild().getNodeValue();

            NodeList nodelist_desc = elementRoot.getElementsByTagName("desc");
            Node nDesc = nodelist_desc.item(0);
            desc = nDesc.getFirstChild().getNodeValue();

            fileInputStream.close();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return (new Route(name, desc));
    }

}

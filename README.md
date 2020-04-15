# capacitor-http
A native http client (that is, request are made outside of the WebView, hence dodging mobile CORS issues) capable of making JSON api requests and supporting SOCKS and HTTP proxies. This is an extension of the https://ionicframework.com/docs/native/http Cordova plugin and the work in https://github.com/ionic-team/capacitor/tree/http-api experimental branch of the capacitor core project.


To install into your ionic project:
```
$ npm i --save capacitor-http
$ npx cap update

... add module into the typescript how you like ...

$ ionic build
$ npx cap sync
$ npx cap open ios && npx cap open android
```

You MUST also edit your android 'android/app/src/main/java/.../MainActivity.java'

```
...
import http.plugin.HttpPlugin; // <-- add this

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initializes the Bridge
    this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
      // Additional plugins you've installed go here
       add(HttpPlugin.class); // <-- add this
    }});
  }
}

```

Sample use in an ionic app:

```
import { Component } from '@angular/core';
import { HttpPluginNativeImpl } from 'capacitor-http'; // <-- import it via 'web' syntax to get type safety in the client code.

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page {
  private readonly torClient = new TorClient();

  httpReply: any

  constructor() {
  }

  async ngOnInit() {
  }

  async testHttplient() {
    this.httpReply = await HttpPluginNativeImpl.request({
      url: 'http://jsonplaceholder.typicode.com/todos/1',
      method: 'GET',
    }).then(JSON.stringify),
  }
}
```
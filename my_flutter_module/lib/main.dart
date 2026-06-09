import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _currentRoute = '/';
  final MethodChannel _navChannel =
      const MethodChannel('com.thundersoft.myapplication/navigation');

  @override
  void initState() {
    super.initState();
    _navChannel.setMethodCallHandler((call) async {
      if (call.method == 'setRoute') {
        setState(() {
          _currentRoute = call.arguments as String;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Module',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.teal,
        useMaterial3: true,
      ),
      home: _getPageForRoute(_currentRoute),
    );
  }

  Widget _getPageForRoute(String route) {
    switch (route) {
      case '/second':
        return const SecondPage();
      default:
        return const HybridHomePage();
    }
  }
}

// ========== 页面1：首页（原有页面）==========
class HybridHomePage extends StatefulWidget {
  const HybridHomePage({super.key});

  @override
  State<HybridHomePage> createState() => _HybridHomePageState();
}

class _HybridHomePageState extends State<HybridHomePage> {
  int _counter = 0;
  String _platformInfo = '';
  final MethodChannel _channel =
      const MethodChannel('com.thundersoft.myapplication/channel');

  @override
  void initState() {
    super.initState();
    _getPlatformInfo();
  }

  Future<void> _getPlatformInfo() async {
    try {
      final String result = await _channel.invokeMethod('getPlatformVersion');
      setState(() {
        _platformInfo = result;
      });
    } catch (e) {
      setState(() {
        _platformInfo = '未知';
      });
    }
  }

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter 页面'),
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            SystemNavigator.pop();
          },
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              color: Colors.teal.shade50,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    const Icon(
                      Icons.flutter_dash,
                      size: 48,
                      color: Colors.teal,
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      '原生 Android + Flutter 混合开发',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '这是一个嵌入到原生 App 中的 Flutter 页面',
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey.shade700,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            const Text(
              '交互演示',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Text(
                      '点击次数: $_counter',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    const SizedBox(height: 12),
                    ElevatedButton.icon(
                      onPressed: _incrementCounter,
                      icon: const Icon(Icons.add),
                      label: const Text('增加计数'),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            const Text(
              '平台信息',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Card(
              child: ListTile(
                leading: const Icon(Icons.phone_android),
                title: const Text('操作系统'),
                subtitle: Text('Android ($_platformInfo)'),
              ),
            ),
            const Spacer(),

            Center(
              child: Text(
                '通过 MethodChannel 可与原生通信',
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey.shade600,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ========== 页面2：第二页（新增）==========
class SecondPage extends StatelessWidget {
  const SecondPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter 第二页'),
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            SystemNavigator.pop();
          },
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              color: Colors.orange.shade50,
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: Column(
                  children: [
                    const Icon(
                      Icons.pages,
                      size: 64,
                      color: Colors.orange,
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      '第二个 Flutter 页面',
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '通过路由 /second 加载的页面',
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey.shade700,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '路由信息',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    Text('当前路由: /second'),
                    const SizedBox(height: 8),
                    Text('这是从原生 Android 通过 initialRoute 传入的'),
                  ],
                ),
              ),
            ),
            const Spacer(),
            Center(
              child: Text(
                '同一个 module，不同路由 = 不同页面',
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey.shade600,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

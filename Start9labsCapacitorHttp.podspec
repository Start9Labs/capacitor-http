
  Pod::Spec.new do |s|
    s.name = 'Start9labsCapacitorHttp'
    s.version = '0.1.1'
    s.summary = 'native ios http requests with socks proxy support'
    s.license = 'MIT'
    s.homepage = 'git@github.com:Start9Labs/capacitor-http.git'
    s.author = 'start9labs'
    s.source = { :git => 'git@github.com:Start9Labs/capacitor-http.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end

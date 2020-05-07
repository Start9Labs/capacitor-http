
  Pod::Spec.new do |s|
    s.name = 'CapacitorHttp'
    s.version = '0.0.1'
    s.summary = 'http plugin with socks + http proxy support'
    s.license = 'MIT'
    s.homepage = 'git@github.com:Start9Labs/http-api-proxy.git'
    s.author = 'AG + KM'
    s.source = { :git => 'git@github.com:Start9Labs/http-api-proxy.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end
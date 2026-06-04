import router from '@ohos.router';
import { HttpUtil } from '../utils/HttpUtil';
import { UserStore } from '../store/UserStore';
import { CommonUtil } from '../utils/CommonUtil';
import common from '@ohos.app.ability.common';

@Entry
@Component
struct Index {
  @State isLoading: boolean = true;

  aboutToAppear() {
    const context = getContext(this) as common.Context;
    HttpUtil.init(context);
    UserStore.getInstance().init(context);
    CommonUtil.init(context);
    
    this.checkLoginStatus();
  }

  async checkLoginStatus() {
    const userStore = UserStore.getInstance();
    
    if (HttpUtil.getUserId()) {
      userStore.userId = HttpUtil.getUserId();
      await userStore.fetchStats();
    }

    setTimeout(() => {
      if (userStore.isLoggedIn) {
        router.replaceUrl({ url: 'pages/Home' });
      } else {
        router.replaceUrl({ url: 'pages/Auth' });
      }
    }, 500);
  }

  build() {
    Column() {
      Text('言外')
        .fontSize(48)
        .fontWeight(FontWeight.Bold)
        .linearGradient({
          angle: 135,
          colors: [['#D4AF37', 0.0], ['#F5D061', 1.0]]
        })
      
      LoadingProgress()
        .width(50)
        .height(50)
        .color('#D4AF37')
        .margin({ top: 40 })
    }
    .width('100%')
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .backgroundColor('#0B0E14')
  }
}

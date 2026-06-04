import router from '@ohos.router';
import { UserStore } from '../store/UserStore';
import { CommonUtil } from '../utils/CommonUtil';
import { UserApi } from '../api/UserApi';
import promptAction from '@ohos.promptAction';
import preferences from '@ohos.data.preferences';
import common from '@ohos.app.ability.common';

@Entry
@Component
struct Profile {
  @State currentTab: number = 2;
  @State isLoggingOut: boolean = false;
  @State isResetting: boolean = false;
  @State isWatchingAd: boolean = false;

  userStore: UserStore = UserStore.getInstance();
  context: common.Context | null = null;

  aboutToAppear() {
    this.context = getContext(this) as common.Context;
    this.loadUserData();
  }

  async loadUserData() {
    await this.userStore.fetchStats();
  }

  async watchAdForMore() {
    this.isWatchingAd = true;
    try {
      await this.userStore.watchAdComplete();
      promptAction.showToast({
        message: '观看成功！已获得更多解码次数',
        duration: 2000
      });
    } catch (e) {
      console.error('Watch ad failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '操作失败，请重试',
        duration: 2000
      });
    } finally {
      this.isWatchingAd = false;
    }
  }

  async resetData() {
    this.isResetting = true;
    try {
      await this.userStore.resetData();
      promptAction.showToast({
        message: '数据已重置',
        duration: 2000
      });
    } catch (e) {
      console.error('Reset data failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '操作失败，请重试',
        duration: 2000
      });
    } finally {
      this.isResetting = false;
    }
  }

  async logout() {
    this.isLoggingOut = true;
    try {
      await this.userStore.logout();
      promptAction.showToast({
        message: '已退出登录',
        duration: 2000
      });
      router.replaceUrl({ url: 'pages/Auth' });
    } catch (e) {
      console.error('Logout failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '操作失败，请重试',
        duration: 2000
      });
    } finally {
      this.isLoggingOut = false;
    }
  }

  build() {
    Stack() {
      Column() {
        Column() {
          Text('个人中心')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .color('#F8FAFC')
            .width('100%')
            .textAlign(TextAlign.Start)
        }
        .width('100%')
        .padding({ top: 16, bottom: 20 })

        Scroll() {
          Column({ space: 16 }) {
            Column() {
              Row() {
                Column() {
                  Text('👤')
                    .fontSize(40)
                }
                .width(72)
                .height(72)
                .borderRadius(36)
                .backgroundColor('rgba(212, 175, 55, 0.2)')
                .justifyContent(FlexAlign.Center)
                .margin({ right: 16 })

                Column() {
                  Text(this.userStore.nickname || '用户')
                    .fontSize(20)
                    .fontWeight(FontWeight.Bold)
                    .color('#F8FAFC')
                    .width('100%')
                    .textAlign(TextAlign.Start)

                  if (this.userStore.memberLevel === 1) {
                    Row() {
                      Text('⭐')
                        .fontSize(14)
                      Text('会员')
                        .fontSize(13)
                        .fontWeight(FontWeight.Medium)
                        .color('#D4AF37')
                    }
                    .padding({ left: 10, right: 10, top: 4, bottom: 4 })
                    .backgroundColor('rgba(212, 175, 55, 0.2)')
                    .borderRadius(12)
                    .margin({ top: 6 })
                  }
                }
                .layoutWeight(1)
                .alignItems(HorizontalAlign.Start)
              }
              .width('100%')
              .alignItems(VerticalAlign.Center)
            }
            .width('100%')
            .padding(20)
            .borderRadius(20)
            .backgroundColor('rgba(30, 41, 59, 0.95)')
            .border({
              width: 1,
              color: 'rgba(212, 175, 55, 0.3)'
            })

            Column() {
              Text('使用统计')
                .fontSize(15)
                .fontWeight(FontWeight.SemiBold)
                .color('#94A3B8')
                .width('100%')
                .textAlign(TextAlign.Start)
                .margin({ bottom: 16 })

              Row() {
                Column() {
                  Text(this.userStore.totalAnalysis.toString())
                    .fontSize(28)
                    .fontWeight(FontWeight.Bold)
                    .linearGradient({
                      angle: 135,
                      colors: [['#D4AF37', 0.0], ['#F5D061', 1.0]]
                    })
                  Text('累计解码')
                    .fontSize(13)
                    .color('#94A3B8')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)

                Column() {
                  Text(this.userStore.totalShare.toString())
                    .fontSize(28)
                    .fontWeight(FontWeight.Bold)
                    .linearGradient({
                      angle: 135,
                      colors: [['#D4AF37', 0.0], ['#F5D061', 1.0]]
                    })
                  Text('分享次数')
                    .fontSize(13)
                    .color('#94A3B8')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)
              }
              .width('100%')

              Row() {
                Column() {
                  Text(this.userStore.romanceCount.toString())
                    .fontSize(28)
                    .fontWeight(FontWeight.Bold)
                    .linearGradient({
                      angle: 135,
                      colors: [['#D4AF37', 0.0], ['#F5D061', 1.0]]
                    })
                  Text('情感分析')
                    .fontSize(13)
                    .color('#94A3B8')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)

                Column() {
                  Text(this.userStore.workplaceCount.toString())
                    .fontSize(28)
                    .fontWeight(FontWeight.Bold)
                    .linearGradient({
                      angle: 135,
                      colors: [['#D4AF37', 0.0], ['#F5D061', 1.0]]
                    })
                  Text('职场分析')
                    .fontSize(13)
                    .color('#94A3B8')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)

                Column() {
                  Text(this.userStore.lateNightCount.toString())
                    .fontSize(28)
                    .fontWeight(FontWeight.Bold)
                    .linearGradient({
                      angle: 135,
                      colors: [['#D4AF37', 0.0], ['#F5D061', 1.0]]
                    })
                  Text('深夜分析')
                    .fontSize(13)
                    .color('#94A3B8')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)
              }
              .width('100%')
              .margin({ top: 16 })
            }
            .width('100%')
            .padding(20)
            .borderRadius(20)
            .backgroundColor('rgba(30, 41, 59, 0.8)')
            .border({
              width: 1,
              color: 'rgba(212, 175, 55, 0.15)'
            })

            Column() {
              Row() {
                Text('今日剩余次数')
                  .fontSize(15)
                  .fontWeight(FontWeight.Medium)
                  .color('#F8FAFC')
                Text(`${this.userStore.remainingAnalysis}/${this.userStore.dailyLimit}`)
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                  .color('#D4AF37')
              }
              .width('100%')
              .justifyContent(FlexAlign.SpaceBetween)
              .margin({ bottom: 12 })

              Progress({ value: this.userStore.remainingAnalysis, total: this.userStore.dailyLimit, type: ProgressType.Linear })
                .width('100%')
                .height(8)
                .color('#D4AF37')
                .backgroundColor('#1E293B')
                .borderRadius(4)

              if (this.userStore.canWatchAd && this.userStore.memberLevel !== 1) {
                Button() {
                  Row({ space: 8 }) {
                    if (this.isWatchingAd) {
                      LoadingProgress()
                        .width(20)
                        .height(20)
                        .color('#D4AF37')
                    } else {
                      Text('🎬')
                        .fontSize(18)
                    }
                    Text(this.isWatchingAd ? '观看中...' : '看广告获得更多次数')
                      .fontSize(15)
                      .fontWeight(FontWeight.Medium)
                  }
                }
                .width('100%')
                .height(48)
                .margin({ top: 16 })
                .backgroundColor('rgba(212, 175, 55, 0.2)')
                .borderRadius(14)
                .fontColor('#D4AF37')
                .enabled(!this.isWatchingAd)
                .onClick(() => {
                  this.watchAdForMore();
                })
              }
            }
            .width('100%')
            .padding(20)
            .borderRadius(20)
            .backgroundColor('rgba(30, 41, 59, 0.8)')
            .border({
              width: 1,
              color: 'rgba(212, 175, 55, 0.15)'
            })

            Column() {
              Button() {
                Row({ space: 8 }) {
                  if (this.isResetting) {
                    LoadingProgress()
                      .width(20)
                      .height(20)
                      .color('#EF4444')
                  } else {
                    Text('🔄')
                      .fontSize(18)
                  }
                  Text(this.isResetting ? '重置中...' : '重置数据')
                    .fontSize(15)
                    .fontWeight(FontWeight.Medium)
                }
              }
              .width('100%')
              .height(48)
              .backgroundColor('rgba(239, 68, 68, 0.15)')
              .borderRadius(14)
              .fontColor('#EF4444')
              .enabled(!this.isResetting)
              .onClick(() => {
                this.resetData();
              })
              .margin({ bottom: 12 })

              Button() {
                Row({ space: 8 }) {
                  if (this.isLoggingOut) {
                    LoadingProgress()
                      .width(20)
                      .height(20)
                      .color('#EF4444')
                  } else {
                    Text('🚪')
                      .fontSize(18)
                  }
                  Text(this.isLoggingOut ? '退出中...' : '退出登录')
                    .fontSize(15)
                    .fontWeight(FontWeight.Medium)
                }
              }
              .width('100%')
              .height(48)
              .backgroundColor('rgba(239, 68, 68, 0.15)')
              .borderRadius(14)
              .fontColor('#EF4444')
              .enabled(!this.isLoggingOut)
              .onClick(() => {
                this.logout();
              })
            }
            .width('100%')
          }
          .width('100%')
          .padding({ bottom: 20 })
        }
        .layoutWeight(1)
        .width('100%')

        Row() {
          ForEach([
            { index: 0, label: '解码', icon: '🔮' },
            { index: 1, label: '收藏', icon: '📚' },
            { index: 2, label: '我的', icon: '👤' }
          ], (tab: { index: number, label: string, icon: string }) => {
            Column({ space: 4 }) {
              Text(tab.icon)
                .fontSize(26)
              Text(tab.label)
                .fontSize(12)
                .fontWeight(FontWeight.SemiBold)
                .letterSpacing(0.3)
            }
            .layoutWeight(1)
            .padding({ top: 10, bottom: 10 })
            .borderRadius(14)
            .color(this.currentTab === tab.index ? '#D4AF37' : '#94A3B8')
            .onClick(() => {
              if (tab.index !== this.currentTab) {
                this.currentTab = tab.index;
                if (tab.index === 0) {
                  router.replaceUrl({ url: 'pages/Home' });
                } else if (tab.index === 1) {
                  router.replaceUrl({ url: 'pages/Collection' });
                }
              }
            })
          })
        }
        .width('100%')
        .height(70)
        .padding({ left: 20, right: 20 })
        .linearGradient({
          angle: 180,
          colors: [['rgba(30, 41, 59, 0.98)', 0.0], ['rgba(18, 24, 38, 0.98)', 1.0]]
        })
        .border({
          width: { top: 1 },
          color: 'rgba(212, 175, 55, 0.2)'
        })
      }
      .width('100%')
      .height('100%')
      .padding({ left: 16, right: 16 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#0B0E14')
  }
}

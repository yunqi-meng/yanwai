import router from '@ohos.router';
import { UserStore } from '../store/UserStore';
import { CardsApi, UserCard } from '../api/CardsApi';
import { AchievementsApi, AchievementDTO } from '../api/AchievementsApi';
import promptAction from '@ohos.promptAction';

@Entry
@Component
struct Collection {
  @State currentTab: number = 1;
  @State activeTab: 'cards' | 'achievements' = 'cards';
  @State userCards: UserCard[] = [];
  @State userAchievements: AchievementDTO[] = [];
  @State isLoading: boolean = false;
  userStore: UserStore = UserStore.getInstance();

  aboutToAppear() {
    this.loadData();
  }

  async loadData() {
    this.isLoading = true;
    try {
      const [cards, achievements] = await Promise.all([
        CardsApi.getUserCards(),
        AchievementsApi.getUser()
      ]);
      this.userCards = cards;
      this.userAchievements = achievements;
    } catch (e) {
      console.error('Load collection data failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '加载失败，请重试',
        duration: 2000
      });
    } finally {
      this.isLoading = false;
    }
  }

  getRarityColor(rarity: number): string {
    switch (rarity) {
      case 1:
        return '#94A3B8';
      case 2:
        return '#22C55E';
      case 3:
        return '#3B82F6';
      case 4:
        return '#D4AF37';
      default:
        return '#94A3B8';
    }
  }

  getRarityLabel(rarity: number): string {
    switch (rarity) {
      case 1:
        return '普通';
      case 2:
        return '稀有';
      case 3:
        return '史诗';
      case 4:
        return '传说';
      default:
        return '普通';
    }
  }

  build() {
    Stack() {
      Column() {
        Column() {
          Text('我的收藏')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .color('#F8FAFC')
            .width('100%')
            .textAlign(TextAlign.Start)
        }
        .width('100%')
        .padding({ top: 16, bottom: 16 })

        Row() {
          Text('卡片收藏')
            .fontSize(15)
            .fontWeight(FontWeight.SemiBold)
            .padding({ left: 20, right: 20, top: 10, bottom: 10 })
            .borderRadius(12)
            .backgroundColor(this.activeTab === 'cards' ? 'rgba(212, 175, 55, 0.3)' : 'transparent')
            .color(this.activeTab === 'cards' ? '#D4AF37' : '#94A3B8')
            .onClick(() => {
              this.activeTab = 'cards';
            })

          Text('成就系统')
            .fontSize(15)
            .fontWeight(FontWeight.SemiBold)
            .padding({ left: 20, right: 20, top: 10, bottom: 10 })
            .borderRadius(12)
            .backgroundColor(this.activeTab === 'achievements' ? 'rgba(212, 175, 55, 0.3)' : 'transparent')
            .color(this.activeTab === 'achievements' ? '#D4AF37' : '#94A3B8')
            .onClick(() => {
              this.activeTab = 'achievements';
            })
        }
        .width('100%')
        .justifyContent(FlexAlign.Center)
        .margin({ bottom: 20 })

        if (this.isLoading) {
          Column() {
            LoadingProgress()
              .width(40)
              .height(40)
              .color('#D4AF37')
            Text('加载中...')
              .fontSize(14)
              .color('#94A3B8')
              .margin({ top: 12 })
          }
          .layoutWeight(1)
          .width('100%')
          .justifyContent(FlexAlign.Center)
        } else {
          if (this.activeTab === 'cards') {
            if (this.userCards.length === 0) {
              Column() {
                Text('📚')
                  .fontSize(60)
                  .opacity(0.6)
                Text('还没有收藏卡片')
                  .fontSize(16)
                  .color('#94A3B8')
                  .margin({ top: 12 })
              }
              .layoutWeight(1)
              .width('100%')
              .justifyContent(FlexAlign.Center)
            } else {
              Scroll() {
                Column({ space: 12 }) {
                  ForEach(this.userCards, (item: UserCard) => {
                    Row() {
                      Column() {
                        Row() {
                          Text(item.emoji || '🎴')
                            .fontSize(32)
                            .margin({ right: 12 })
                          Column() {
                            Text(item.name || '未知卡片')
                              .fontSize(16)
                              .fontWeight(FontWeight.SemiBold)
                              .color('#F8FAFC')
                              .width('100%')
                              .textAlign(TextAlign.Start)
                            Row() {
                              Text(this.getRarityLabel(item.rarity))
                                .fontSize(12)
                                .fontWeight(FontWeight.Medium)
                                .color(this.getRarityColor(item.rarity))
                                .padding({ left: 8, right: 8, top: 4, bottom: 4 })
                                .backgroundColor(`${this.getRarityColor(item.rarity)}20`)
                                .borderRadius(8)
                            }
                            .margin({ top: 4 })
                          }
                          .layoutWeight(1)
                          .alignItems(HorizontalAlign.Start)
                        }
                        .width('100%')
                        .alignItems(VerticalAlign.Center)
                        if (item.description) {
                          Text(item.description)
                            .fontSize(13)
                            .color('#94A3B8')
                            .width('100%')
                            .textAlign(TextAlign.Start)
                            .margin({ top: 8 })
                        }
                      }
                      .layoutWeight(1)
                      .alignItems(HorizontalAlign.Start)

                      Column() {
                        Text(`× ${item.quantity}`)
                          .fontSize(18)
                          .fontWeight(FontWeight.Bold)
                          .color('#D4AF37')
                      }
                      .alignItems(HorizontalAlign.Center)
                    }
                    .width('100%')
                    .padding(16)
                    .borderRadius(16)
                    .backgroundColor('rgba(30, 41, 59, 0.8)')
                    .border({
                      width: 1,
                      color: 'rgba(212, 175, 55, 0.15)'
                    })
                  })
                }
                .width('100%')
              }
              .layoutWeight(1)
              .width('100%')
            }
          } else {
            if (this.userAchievements.length === 0) {
              Column() {
                Text('🏆')
                  .fontSize(60)
                  .opacity(0.6)
                Text('还没有解锁成就')
                  .fontSize(16)
                  .color('#94A3B8')
                  .margin({ top: 12 })
              }
              .layoutWeight(1)
              .width('100%')
              .justifyContent(FlexAlign.Center)
            } else {
              Scroll() {
                Column({ space: 12 }) {
                  ForEach(this.userAchievements, (item: AchievementDTO) => {
                    Row() {
                      Column() {
                        Row() {
                          Text(item.unlocked ? '🏆' : '🔒')
                            .fontSize(24)
                            .margin({ right: 10 })
                          Text(item.name || '未知成就')
                            .fontSize(16)
                            .fontWeight(FontWeight.SemiBold)
                            .color(item.unlocked ? '#F8FAFC' : '#64748B')
                        }
                        .width('100%')
                        .alignItems(VerticalAlign.Center)

                        Text(item.description || '')
                          .fontSize(13)
                          .color('#94A3B8')
                          .width('100%')
                          .textAlign(TextAlign.Start)
                          .margin({ top: 4 })
                      }
                      .layoutWeight(1)
                      .alignItems(HorizontalAlign.Start)
                    }
                    .width('100%')
                    .padding(16)
                    .borderRadius(16)
                    .backgroundColor(item.unlocked ? 'rgba(30, 41, 59, 0.95)' : 'rgba(30, 41, 59, 0.6)')
                    .border({
                      width: 1,
                      color: item.unlocked ? 'rgba(212, 175, 55, 0.3)' : 'rgba(212, 175, 55, 0.1)'
                    })
                  })
                }
                .width('100%')
              }
              .layoutWeight(1)
              .width('100%')
            }
          }
        }

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
                } else if (tab.index === 2) {
                  router.replaceUrl({ url: 'pages/Profile' });
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

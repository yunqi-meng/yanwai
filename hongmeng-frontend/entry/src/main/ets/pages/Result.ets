import router from '@ohos/router';
import { CommonUtil, DecodeResult } from '../utils/CommonUtil';
import { UserStore } from '../store/UserStore';
import { AnalysisApi } from '../api/AnalysisApi';
import promptAction from '@ohos/promptAction';

interface EmotionPoint {
  index?: number;
  speaker?: string;
  emotionScore?: number;
  emotionType?: string;
  description?: string;
}

interface TranslationItem {
  index?: number;
  original?: string;
  literal?: string;
  subtext?: string;
  emotionScore?: number;
}

interface AnalysisResultDTO {
  relationship?: string;
  emotionCurve?: EmotionPoint[];
  translations?: TranslationItem[];
  advice?: string[];
}

@Entry
@Component
struct Result {
  @State currentTab: number = 0;
  @State decodeResult: DecodeResult | null = null;
  @State isSharing: boolean = false;

  userStore: UserStore = UserStore.getInstance();

  aboutToAppear() {
    this.decodeResult = CommonUtil.getDecodeResult();
  }

  async shareResult() {
    this.isSharing = true;
    try {
      await AnalysisApi.share();
      await this.userStore.fetchStats();
      promptAction.showToast({
        message: '分享成功！',
        duration: 2000
      });
    } catch (e) {
      console.error('Share failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '分享失败，请重试',
        duration: 2000
      });
    } finally {
      this.isSharing = false;
    }
  }

  getAnalysisText(analysis: any): string {
    if (!analysis) return '暂无分析结果';
    if (typeof analysis === 'string') return analysis;
    if (typeof analysis === 'object') {
      const result: string[] = [];
      if (analysis.relationship) {
        result.push('【关系分析】\n' + analysis.relationship);
      }
      if (analysis.translations && analysis.translations.length > 0) {
        result.push('\n【潜台词解读】');
        analysis.translations.forEach((item: TranslationItem, idx: number) => {
          if (item.original) {
            result.push(`\n${idx + 1}. 原文: ${item.original}`);
            if (item.literal) {
              result.push(`   字面: ${item.literal}`);
            }
            if (item.subtext) {
              result.push(`   潜台词: ${item.subtext}`);
            }
          }
        });
      }
      if (analysis.advice && analysis.advice.length > 0) {
        result.push('\n【建议】');
        analysis.advice.forEach((adv: string, idx: number) => {
          result.push(`${idx + 1}. ${adv}`);
        });
      }
      return result.length > 0 ? result.join('\n') : JSON.stringify(analysis, null, 2);
    }
    return JSON.stringify(analysis, null, 2);
  }

  build() {
    Stack() {
      Column() {
        Scroll() {
          Column({ space: 20 }) {
            Column() {
              Text('解码结果')
                .fontSize(24)
                .fontWeight(FontWeight.Bold)
                .color('#F8FAFC')
                .width('100%')
                .textAlign(TextAlign.Start)
            }
            .width('100%')
            .padding({ top: 16 })

            if (this.decodeResult) {
              Column({ space: 20 }) {
                if (this.decodeResult.originalImage) {
                  Column() {
                    Text('原始图片')
                      .fontSize(14)
                      .fontWeight(FontWeight.SemiBold)
                      .color('#94A3B8')
                      .width('100%')
                      .textAlign(TextAlign.Start)
                      .margin({ bottom: 12 })
                    Image(this.decodeResult.originalImage)
                      .width('100%')
                      .maxHeight(400)
                      .objectFit(ImageFit.Contain)
                      .borderRadius(12)
                  }
                  .width('100%')
                  .padding(20)
                  .borderRadius(20)
                  .backgroundColor('rgba(30, 41, 59, 0.8)')
                  .border({
                    width: 1,
                    color: 'rgba(212, 175, 55, 0.15)'
                  })
                }

                Column() {
                  Text('原始对话')
                    .fontSize(14)
                    .fontWeight(FontWeight.SemiBold)
                    .color('#94A3B8')
                    .width('100%')
                    .textAlign(TextAlign.Start)
                    .margin({ bottom: 12 })

                  Text(this.decodeResult.text)
                    .fontSize(15)
                    .lineHeight(26)
                    .color('#F8FAFC')
                    .width('100%')
                    .textAlign(TextAlign.Start)
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
                  Text('解读分析')
                    .fontSize(14)
                    .fontWeight(FontWeight.SemiBold)
                    .color('#94A3B8')
                    .width('100%')
                    .textAlign(TextAlign.Start)
                    .margin({ bottom: 12 })

                  Text(this.getAnalysisText(this.decodeResult.analysis))
                    .fontSize(15)
                    .lineHeight(26)
                    .color('#F8FAFC')
                    .width('100%')
                    .textAlign(TextAlign.Start)
                }
                .width('100%')
                .padding(20)
                .borderRadius(20)
                .backgroundColor('rgba(30, 41, 59, 0.8)')
                .border({
                  width: 1,
                  color: 'rgba(212, 175, 55, 0.15)'
                })

                if (this.decodeResult.newCard) {
                  Column() {
                    Text('🎉 获得新卡片！')
                      .fontSize(18)
                      .fontWeight(FontWeight.Bold)
                      .color('#D4AF37')
                      .width('100%')
                      .textAlign(TextAlign.Center)
                      .margin({ bottom: 16 })

                    Column() {
                      Row() {
                        Text(this.decodeResult.newCard.emoji || '🎴')
                          .fontSize(32)
                          .margin({ right: 12 })
                        Column() {
                          Text(this.decodeResult.newCard.name || '未知卡片')
                            .fontSize(20)
                            .fontWeight(FontWeight.Bold)
                            .color('#F8FAFC')
                          Text(this.decodeResult.newCard.description || '')
                            .fontSize(14)
                            .color('#94A3B8')
                            .margin({ top: 8 })
                        }
                        .layoutWeight(1)
                        .alignItems(HorizontalAlign.Start)
                      }
                      .width('100%')
                    }
                    .width('100%')
                    .padding(24)
                    .borderRadius(16)
                    .linearGradient({
                      angle: 135,
                      colors: [['rgba(212, 175, 55, 0.3)', 0.0], ['rgba(212, 175, 55, 0.1)', 1.0] as const] as const
                    })
                    .border({
                      width: 1,
                      color: 'rgba(212, 175, 55, 0.4)'
                    })
                  }
                  .width('100%')
                  .padding(20)
                  .borderRadius(20)
                  .backgroundColor('rgba(30, 41, 59, 0.95)')
                  .border({
                    width: 1,
                    color: 'rgba(212, 175, 55, 0.3)'
                  })
                }

                if (this.decodeResult.newAchievements && this.decodeResult.newAchievements.length > 0) {
                  Column() {
                    Text('🏆 解锁新成就！')
                      .fontSize(18)
                      .fontWeight(FontWeight.Bold)
                      .color('#D4AF37')
                      .width('100%')
                      .textAlign(TextAlign.Center)
                      .margin({ bottom: 16 })

                    ForEach(this.decodeResult.newAchievements, (achievement: any) => {
                      Column() {
                        Text(achievement.name || '未知成就')
                          .fontSize(16)
                          .fontWeight(FontWeight.SemiBold)
                          .color('#F8FAFC')
                        Text(achievement.description || '')
                          .fontSize(13)
                          .color('#94A3B8')
                          .margin({ top: 4 })
                      }
                      .width('100%')
                      .padding(16)
                      .borderRadius(12)
                      .backgroundColor('rgba(30, 41, 59, 0.6)')
                      .border({
                        width: 1,
                        color: 'rgba(212, 175, 55, 0.2)'
                      })
                      .margin({ bottom: 8 })
                    })
                  }
                  .width('100%')
                  .padding(20)
                  .borderRadius(20)
                  .backgroundColor('rgba(30, 41, 59, 0.95)')
                  .border({
                    width: 1,
                    color: 'rgba(212, 175, 55, 0.3)'
                  })
                }
              }
              .width('100%')
            }

            Button() {
              Row({ space: 8 }) {
                if (this.isSharing) {
                  LoadingProgress()
                    .width(20)
                    .height(20)
                    .color('#0B0E14')
                } else {
                  Text('📤')
                    .fontSize(20)
                }
                Text(this.isSharing ? '分享中...' : '分享获得更多次数')
                  .fontSize(17)
                  .fontWeight(FontWeight.SemiBold)
              }
            }
            .width('100%')
            .height(56)
            .fontColor('#0B0E14')
            .linearGradient({
              angle: 135,
              colors: [['#B8860B', 0.0], ['#D4AF37', 1.0] as const] as const
            })
            .borderRadius(16)
            .enabled(!this.isSharing)
            .margin({ top: 8, bottom: 16 })
            .onClick(() => {
              this.shareResult();
            })
          }
          .padding(16, 16, 96)
          .width('100%')
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
          colors: [['rgba(30, 41, 59, 0.98)', 0.0], ['rgba(18, 24, 38, 0.98)', 1.0] as const] as const
        })
        .border({
          width: { top: 1 },
          color: 'rgba(212, 175, 55, 0.2)'
        })
      }
      .width('100%')
      .height('100%')
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#0B0E14')
  }
}

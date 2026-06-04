import router from '@ohos.router';
import { UserStore } from '../store/UserStore';
import { CommonUtil, DecodeResult } from '../utils/CommonUtil';
import { AnalysisApi, HistoryItem } from '../api/AnalysisApi';
import promptAction from '@ohos.promptAction';
import picker from '@ohos.file.picker';
import fs from '@ohos.file.fs';
import util from '@ohos.util';

@Entry
@Component
struct Home {
  @State currentTab: number = 0;
  @State dialogText: string = '';
  @State isLoading: boolean = false;
  @State loadingProgress: number = 0;
  @State historyList: HistoryItem[] = [];
  @State historyLoading: boolean = false;
  @State selectedImage: string | null = null;
  @State isAnalyzingImage: boolean = false;

  userStore: UserStore = UserStore.getInstance();

  aboutToAppear() {
    this.loadUserStats();
    this.loadHistory();
  }

  async loadUserStats() {
    await this.userStore.guestLogin();
    await this.userStore.fetchStats();
  }

  async loadHistory() {
    this.historyLoading = true;
    try {
      const result = await AnalysisApi.getHistory(1, 10);
      this.historyList = result.records || [];
    } catch (e) {
      console.error('Load history failed:', JSON.stringify(e));
    } finally {
      this.historyLoading = false;
    }
  }

  async triggerUpload() {
    try {
      const photoPicker = new picker.PhotoViewPicker();
      const result = await photoPicker.select({
        MIMEType: picker.PhotoViewMIMETypes.IMAGE_TYPE,
        maxSelectNumber: 1
      });

      if (result.photoUris && result.photoUris.length > 0) {
        this.selectedImage = result.photoUris[0];
        await this.analyzeImage(result.photoUris[0]);
      }
    } catch (e) {
      console.error('Pick photo failed:', JSON.stringify(e));
    }
  }

  async analyzeImage(fileUri: string) {
    this.isAnalyzingImage = true;
    this.isLoading = true;
    
    try {
      const file = fs.openSync(fileUri, fs.OpenMode.READ_ONLY);
      const stat = fs.statSync(fileUri);
      const buffer = new ArrayBuffer(stat.size as number);
      fs.readSync(file.fd, buffer);
      fs.closeSync(file.fd);
      
      const uint8Array = new Uint8Array(buffer);
      const base64 = this.arrayBufferToBase64(uint8Array);
      
      const result = await AnalysisApi.decodeWithImage(base64, this.dialogText);
      this.userStore.incrementDailyCount();
      
      const decodeResult: DecodeResult = {
        text: this.dialogText || '[图片分析]',
        analysis: result.analysis,
        newCard: result.newCard,
        newAchievements: result.newAchievements,
        originalImage: fileUri
      };
      
      CommonUtil.setDecodeResult(decodeResult);
      await new Promise(resolve => setTimeout(resolve, 300));
      router.pushUrl({ url: 'pages/Result' });
    } catch (e) {
      console.error('Image analyze failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '图片分析失败，请重试',
        duration: 2000
      });
    } finally {
      this.isAnalyzingImage = false;
      this.isLoading = false;
    }
  }

  arrayBufferToBase64(uint8Array: Uint8Array): string {
    return util.base64EncodeToString(uint8Array);
  }

  removeImage() {
    this.selectedImage = null;
  }

  async startDecode() {
    if (!this.dialogText.trim()) {
      promptAction.showToast({
        message: '请输入对话内容',
        duration: 2000
      });
      return;
    }

    if (this.userStore.remainingAnalysis <= 0) {
      promptAction.showToast({
        message: '今日免费解码次数已用完',
        duration: 2000
      });
      return;
    }

    this.isLoading = true;
    this.loadingProgress = 0;

    const progressInterval = setInterval(() => {
      this.loadingProgress += Math.random() * 15;
      if (this.loadingProgress > 90) this.loadingProgress = 90;
    }, 300);

    try {
      const result = await AnalysisApi.decode(this.dialogText);
      this.loadingProgress = 100;
      this.userStore.incrementDailyCount();

      const decodeResult: DecodeResult = {
        text: this.dialogText,
        analysis: result.analysis,
        newCard: result.newCard,
        newAchievements: result.newAchievements
      };

      CommonUtil.setDecodeResult(decodeResult);
      
      await new Promise(resolve => setTimeout(resolve, 300));
      
      router.pushUrl({ url: 'pages/Result' });
    } catch (e) {
      console.error('Decode failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '解码失败，请重试',
        duration: 2000
      });
    } finally {
      clearInterval(progressInterval);
      this.isLoading = false;
      this.loadingProgress = 0;
    }
  }

  async loadHistoryDetail(item: HistoryItem) {
    if (item.analysisResult) {
      try {
        const decodeResult: DecodeResult = {
          text: item.originalText,
          analysis: item.analysisResult,
          newCard: null,
          newAchievements: []
        };
        CommonUtil.setDecodeResult(decodeResult);
        router.pushUrl({ url: 'pages/Result' });
      } catch (e) {
        this.dialogText = item.originalText || '';
      }
    } else {
      this.dialogText = item.originalText || '';
    }
  }

  async deleteHistoryItem(id: number) {
    try {
      await AnalysisApi.deleteHistory(id);
      promptAction.showToast({
        message: '删除成功',
        duration: 2000
      });
      await this.loadHistory();
    } catch (e) {
      console.error('Delete history failed:', JSON.stringify(e));
      promptAction.showToast({
        message: '删除失败，请重试',
        duration: 2000
      });
    }
  }

  build() {
    Stack() {
      Column() {
        Scroll() {
          Column({ space: 20 }) {
            Column() {
              Row({ space: 8 }) {
                Text('')
                  .width(6)
                  .height(6)
                  .borderRadius(3)
                  .linearGradient({
                    angle: 135,
                    colors: [['#D4AF37', 0.0], ['#F5D061', 1.0] as const] as const
                  })
                  .opacity(0.4)
                Text('')
                  .width(6)
                  .height(6)
                  .borderRadius(3)
                  .linearGradient({
                    angle: 135,
                    colors: [['#D4AF37', 0.0], ['#F5D061', 1.0] as const] as const
                  })
                  .opacity(0.4)
                Text('')
                  .width(6)
                  .height(6)
                  .borderRadius(3)
                  .linearGradient({
                    angle: 135,
                    colors: [['#D4AF37', 0.0], ['#F5D061', 1.0] as const] as const
                  })
                  .opacity(0.4)
              }
              .margin({ bottom: 16 })

              Text('言外')
                .fontSize(48)
                .fontWeight(FontWeight.Bold)
                .linearGradient({
                  angle: 135,
                  colors: [['#D4AF37', 0.0], ['#F5D061', 1.0] as const] as const
                })

              Text('读懂TA的潜台词')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .color('#94A3B8')
                .margin({ top: 4 })
            }
            .padding({ top: 24, bottom: 28 })

            Column() {
              if (this.selectedImage) {
                Stack({ alignContent: Alignment.TopEnd }) {
                  Image(this.selectedImage)
                    .width('100%')
                    .maxHeight(240)
                    .objectFit(ImageFit.Contain)
                    .borderRadius(12)

                  Button() {
                    Text('✕')
                      .fontSize(16)
                      .fontColor('#FFFFFF')
                  }
                  .width(36)
                  .height(36)
                  .borderRadius(18)
                  .backgroundColor('rgba(239, 68, 68, 0.9)')
                  .margin({ top: 8, right: 8 })
                  .onClick(() => {
                    this.removeImage();
                  })

                  if (this.isAnalyzingImage) {
                    Row({ space: 10 }) {
                      LoadingProgress()
                        .width(20)
                        .height(20)
                        .color('#D4AF37')
                      Text('分析中...')
                        .fontSize(14)
                        .fontWeight(FontWeight.Medium)
                        .color('#D4AF37')
                    }
                    .padding(12)
                  }
                }
                .width('100%')
                .borderRadius(20)
                .padding(16)
                .backgroundColor('rgba(30, 41, 59, 0.8)')
                .border({
                  width: 1,
                  color: 'rgba(212, 175, 55, 0.15)'
                })
              } else {
                Column() {
                  Column() {
                    Text('📷')
                      .fontSize(40)
                  }
                  .width(80)
                  .height(80)
                  .justifyContent(FlexAlign.Center)
                  .borderRadius(24)
                  .backgroundColor('rgba(212, 175, 55, 0.1)')
                  .border({
                    width: 2,
                    color: 'rgba(212, 175, 55, 0.2)'
                  })
                  .margin({ bottom: 16 })

                  Text('点击上传聊天截图')
                    .fontSize(17)
                    .fontWeight(FontWeight.SemiBold)
                    .color('#F8FAFC')
                    .margin({ bottom: 6 })

                  Text('支持识别微信、QQ等聊天界面')
                    .fontSize(13)
                    .color('#64748B')
                }
                .width('100%')
                .padding(40, 24)
                .borderRadius(20)
                .backgroundColor('rgba(30, 41, 59, 0.8)')
                .border({
                  width: 2,
                  style: BorderStyle.Dashed,
                  color: 'rgba(212, 175, 55, 0.2)'
                })
                .onClick(() => {
                  this.triggerUpload();
                })
              }
            }

            Row() {
              Divider()
                .layoutWeight(1)
                .linearGradient({
                  angle: 90,
                  colors: [['rgba(212, 175, 55, 0)', 0.0], ['rgba(212, 175, 55, 0.2)', 0.5], ['rgba(212, 175, 55, 0)', 1.0] as const] as const
                })

              Text('或')
                .fontSize(13)
                .color('#64748B')
                .margin({ left: 16, right: 16 })

              Divider()
                .layoutWeight(1)
                .linearGradient({
                  angle: 90,
                  colors: [['rgba(212, 175, 55, 0)', 0.0], ['rgba(212, 175, 55, 0.2)', 0.5], ['rgba(212, 175, 55, 0)', 1.0] as const] as const
                })
            }

            Column() {
              TextArea({ placeholder: '粘贴对话内容...\n\n例如：\nA: 今天天气真好\nB: 是啊\nA: 我们出去走走吧\nB: 随便', text: this.dialogText })
                .width('100%')
                .height(180)
                .fontSize(15)
                .fontColor('#F8FAFC')
                .backgroundColor('#1E293B')
                .borderRadius(16)
                .border({
                  width: 1,
                  color: 'rgba(212, 175, 55, 0.2)'
                })
                .padding(18, 16)
                .onChange((value: string) => {
                  this.dialogText = value;
                })
            }

            Column() {
              Row() {
                Text(this.userStore.remainingAnalysis.toString())
                  .fontSize(22)
                  .fontWeight(FontWeight.Bold)
                  .color('#D4AF37')
                Text(`/${this.userStore.dailyLimit}次今日解码`)
                  .fontSize(14)
                  .color('#94A3B8')
                if (this.userStore.memberLevel === 1) {
                  Text('⭐会员')
                    .fontSize(12)
                    .color('#D4AF37')
                    .margin({ left: 8 })
                }
              }
            }

            if (this.historyList.length > 0 || this.historyLoading) {
              Column({ space: 12 }) {
                Text('最近解码')
                  .fontSize(14)
                  .fontWeight(FontWeight.SemiBold)
                  .color('#94A3B8')
                  .width('100%')
                  .textAlign(TextAlign.Start)

                if (this.historyLoading) {
                  Row() {
                    LoadingProgress()
                      .width(24)
                      .height(24)
                      .color('#D4AF37')
                  }
                  .width('100%')
                  .justifyContent(FlexAlign.Center)
                  .padding(24)
                } else if (this.historyList.length === 0) {
                  Column() {
                    Text('📝')
                      .fontSize(40)
                      .opacity(0.6)
                    Text('暂无解码记录，开始你的第一次分析吧！')
                      .fontSize(14)
                      .color('#94A3B8')
                      .margin({ top: 10 })
                  }
                  .width('100%')
                  .padding(36, 20)
                  .backgroundColor('rgba(30, 41, 59, 0.4)')
                  .borderRadius(16)
                  .border({
                    width: 1,
                    style: BorderStyle.Dashed,
                    color: 'rgba(212, 175, 55, 0.1)'
                  })
                } else {
                  ForEach(this.historyList.slice(0, 3), (item: HistoryItem) => {
                    Row() {
                      Column({ space: 8 }) {
                        Text(item.originalText.length > 60 ? item.originalText.substring(0, 60) + '...' : item.originalText)
                          .fontSize(14)
                          .fontWeight(FontWeight.Medium)
                          .color('#F8FAFC')
                          .width('100%')
                          .textAlign(TextAlign.Start)
                          .maxLines(2)
                          .textOverflow({ overflow: TextOverflow.Ellipsis })

                        Row() {
                          if (item.relationship) {
                            Text(item.relationship)
                              .fontSize(12)
                              .fontWeight(FontWeight.Medium)
                              .color('#D4AF37')
                              .padding(4, 10)
                              .backgroundColor('rgba(212, 175, 55, 0.1)')
                              .borderRadius(12)
                          }
                          Text(CommonUtil.formatTime(item.createdAt))
                            .fontSize(12)
                            .color('#64748B')
                            .layoutWeight(1)
                            .textAlign(TextAlign.End)
                        }
                        .width('100%')
                        .justifyContent(FlexAlign.SpaceBetween)
                      }
                      .layoutWeight(1)
                      .onClick(() => {
                        this.loadHistoryDetail(item);
                      })

                      Button() {
                        Text('🗑️')
                          .fontSize(16)
                      }
                      .width(40)
                      .height(40)
                      .backgroundColor('transparent')
                      .onClick(() => {
                        this.deleteHistoryItem(item.id);
                      })
                    }
                    .width('100%')
                    .padding(14)
                    .borderRadius(16)
                    .backgroundColor('rgba(18, 24, 38, 0.95)')
                    .border({
                      width: 1,
                      color: 'rgba(212, 175, 55, 0.1)'
                    })
                  })
                }
              }
              .width('100%')
            }

            Button() {
              Row({ space: 8 }) {
                if (this.isLoading) {
                  LoadingProgress()
                    .width(20)
                    .height(20)
                    .color('#0B0E14')
                } else {
                  Text('🔮')
                    .fontSize(20)
                }
                Text(this.isLoading ? '解码中...' : '开始解码')
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
            .enabled(!this.isLoading && (this.dialogText.trim().length > 0 || this.selectedImage) && this.userStore.remainingAnalysis > 0)
            .opacity((!this.isLoading && (this.dialogText.trim().length > 0 || this.selectedImage) && this.userStore.remainingAnalysis > 0) ? 1 : 0.5)
            .onClick(() => {
              if (this.selectedImage) {
                this.analyzeImage(this.selectedImage);
              } else {
                this.startDecode();
              }
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
                if (tab.index === 1) {
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

      if (this.isLoading && !this.isAnalyzingImage) {
        Column() {
          Text('🔮')
            .fontSize(90)
          
          Text('正在解读潜台词...')
            .fontSize(18)
            .fontWeight(FontWeight.Medium)
            .color('#D4AF37')
            .letterSpacing(1)
            .margin({ top: 24 })
          
          Progress({ value: this.loadingProgress, total: 100, type: ProgressType.Linear })
            .width(200)
            .height(6)
            .color('#D4AF37')
            .backgroundColor('#1E293B')
            .margin({ top: 24 })
        }
        .width('100%')
        .height('100%')
        .backgroundColor('rgba(0, 0, 0, 0.9)')
        .justifyContent(FlexAlign.Center)
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#0B0E14')
  }
}

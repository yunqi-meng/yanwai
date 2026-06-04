import router from '@ohos.router';
import { UserStore } from '../store/UserStore';
import { CommonUtil } from '../utils/CommonUtil';
import promptAction from '@ohos.promptAction';

@Entry
@Component
struct Auth {
  @State isLogin: boolean = true;
  @State email: string = '';
  @State password: string = '';
  @State confirmPassword: string = '';
  @State showPassword: boolean = false;
  @State isLoading: boolean = false;
  @State emailError: string = '';
  @State passwordError: string = '';
  @State confirmPasswordError: string = '';

  userStore: UserStore = UserStore.getInstance();

  validateForm(): boolean {
    this.emailError = '';
    this.passwordError = '';
    this.confirmPasswordError = '';

    if (!this.email.trim()) {
      this.emailError = '请输入邮箱';
      return false;
    }
    if (!CommonUtil.validateEmail(this.email)) {
      this.emailError = '请输入有效的邮箱地址';
      return false;
    }

    if (!this.password.trim()) {
      this.passwordError = '请输入密码';
      return false;
    }
    const pwdValidation = CommonUtil.validatePassword(this.password);
    if (!pwdValidation.valid) {
      this.passwordError = pwdValidation.message;
      return false;
    }

    if (!this.isLogin) {
      if (!this.confirmPassword.trim()) {
        this.confirmPasswordError = '请确认密码';
        return false;
      }
      if (this.password !== this.confirmPassword) {
        this.confirmPasswordError = '两次输入的密码不一致';
        return false;
      }
    }

    return true;
  }

  async handleSubmit() {
    if (!this.validateForm()) return;

    this.isLoading = true;
    try {
      let success = false;
      if (this.isLogin) {
        success = await this.userStore.login(this.email, this.password);
      } else {
        success = await this.userStore.register(this.email, this.password);
      }

      if (success) {
        promptAction.showToast({
          message: this.isLogin ? '登录成功' : '注册成功',
          duration: 2000
        });
        router.replaceUrl({ url: 'pages/Home' });
      } else {
        promptAction.showToast({
          message: this.isLogin ? '登录失败，邮箱或密码错误' : '注册失败，邮箱已被注册',
          duration: 2000
        });
      }
    } catch (e) {
      promptAction.showToast({
        message: '网络错误，请稍后重试',
        duration: 2000
      });
    } finally {
      this.isLoading = false;
    }
  }

  async guestLogin() {
    this.isLoading = true;
    try {
      const success = await this.userStore.guestLogin();
      if (success) {
        router.replaceUrl({ url: 'pages/Home' });
      } else {
        promptAction.showToast({
          message: '访客登录失败',
          duration: 2000
        });
      }
    } catch (e) {
      promptAction.showToast({
        message: '网络错误，请稍后重试',
        duration: 2000
      });
    } finally {
      this.isLoading = false;
    }
  }

  build() {
    Stack() {
      Column() {
        Column() {
          Text('🔮')
            .fontSize(64)
            .margin({ bottom: 12 })
          
          Text('言外')
            .fontSize(32)
            .fontWeight(FontWeight.Bold)
            .linearGradient({
              angle: 135,
              colors: [['#D4AF37', 0.0], ['#F5D061', 1.0]]
            })
          
          Text('听懂TA的潜台词')
            .fontSize(14)
            .color('#64748B')
            .margin({ top: 4 })
        }
        .margin({ bottom: 32 })

        Row() {
          Text('登录')
            .fontSize(15)
            .fontWeight(FontWeight.Medium)
            .width('50%')
            .textAlign(TextAlign.Center)
            .padding({ top: 12, bottom: 12 })
            .borderRadius({ bottomLeft: this.isLogin ? 12 : 0, topLeft: this.isLogin ? 12 : 0 })
            .backgroundColor(this.isLogin ? 'rgba(212, 175, 55, 0.3)' : 'transparent')
            .color(this.isLogin ? '#D4AF37' : '#64748B')
            .onClick(() => {
              this.isLogin = true;
            })

          Text('注册')
            .fontSize(15)
            .fontWeight(FontWeight.Medium)
            .width('50%')
            .textAlign(TextAlign.Center)
            .padding({ top: 12, bottom: 12 })
            .borderRadius({ bottomRight: !this.isLogin ? 12 : 0, topRight: !this.isLogin ? 12 : 0 })
            .backgroundColor(!this.isLogin ? 'rgba(212, 175, 55, 0.3)' : 'transparent')
            .color(!this.isLogin ? '#D4AF37' : '#64748B')
            .onClick(() => {
              this.isLogin = false;
            })
        }
        .width('100%')
        .borderRadius(12)
        .backgroundColor('rgba(30, 41, 59, 0.5)')
        .margin({ bottom: 32 })

        Column({ space: 20 }) {
          Column({ space: 8 }) {
            Text('邮箱')
              .fontSize(13)
              .fontWeight(FontWeight.Medium)
              .color('#94A3B8')
              .width('100%')
              .textAlign(TextAlign.Start)

            Row() {
              Text('📧')
                .fontSize(16)
                .margin({ right: 10 })

              TextInput({ text: this.email, placeholder: '请输入邮箱地址' })
                .layoutWeight(1)
                .fontSize(15)
                .fontColor('#F8FAFC')
                .backgroundColor('rgba(30, 41, 59, 0.8)')
                .borderRadius(12)
                .border({
                  width: 1,
                  color: this.emailError ? '#EF4444' : 'rgba(212, 175, 55, 0.2)'
                })
                .onChange((value: string) => {
                  this.email = value;
                  this.emailError = '';
                })
            }
            .padding({ left: 14, right: 14, top: 12, bottom: 12 })
            .width('100%')

            if (this.emailError) {
              Text(this.emailError)
                .fontSize(12)
                .color('#EF4444')
                .width('100%')
                .textAlign(TextAlign.Start)
            }
          }

          Column({ space: 8 }) {
            Text('密码')
              .fontSize(13)
              .fontWeight(FontWeight.Medium)
              .color('#94A3B8')
              .width('100%')
              .textAlign(TextAlign.Start)

            Row() {
              Text('🔐')
                .fontSize(16)
                .margin({ right: 10 })

              TextInput({ text: this.password, placeholder: '请输入密码' })
                .layoutWeight(1)
                .type(this.showPassword ? InputType.Normal : InputType.Password)
                .fontSize(15)
                .fontColor('#F8FAFC')
                .backgroundColor('rgba(30, 41, 59, 0.8)')
                .borderRadius(12)
                .border({
                  width: 1,
                  color: this.passwordError ? '#EF4444' : 'rgba(212, 175, 55, 0.2)'
                })
                .onChange((value: string) => {
                  this.password = value;
                  this.passwordError = '';
                })

              Text(this.showPassword ? '🙈' : '👁️')
                .fontSize(16)
                .margin({ left: 10 })
                .onClick(() => {
                  this.showPassword = !this.showPassword;
                })
            }
            .padding({ left: 14, right: 14, top: 12, bottom: 12 })
            .width('100%')

            if (this.passwordError) {
              Text(this.passwordError)
                .fontSize(12)
                .color('#EF4444')
                .width('100%')
                .textAlign(TextAlign.Start)
            }

            if (!this.isLogin) {
              Text('密码需包含字母和数字，至少6位')
                .fontSize(11)
                .color('#64748B')
                .width('100%')
                .textAlign(TextAlign.Start)
            }
          }

          if (!this.isLogin) {
            Column({ space: 8 }) {
              Text('确认密码')
                .fontSize(13)
                .fontWeight(FontWeight.Medium)
                .color('#94A3B8')
                .width('100%')
                .textAlign(TextAlign.Start)

              Row() {
                Text('🔑')
                  .fontSize(16)
                  .margin({ right: 10 })

                TextInput({ text: this.confirmPassword, placeholder: '请再次输入密码' })
                  .layoutWeight(1)
                  .type(this.showPassword ? InputType.Normal : InputType.Password)
                  .fontSize(15)
                  .fontColor('#F8FAFC')
                  .backgroundColor('rgba(30, 41, 59, 0.8)')
                  .borderRadius(12)
                  .border({
                    width: 1,
                    color: this.confirmPasswordError ? '#EF4444' : 'rgba(212, 175, 55, 0.2)'
                  })
                  .onChange((value: string) => {
                    this.confirmPassword = value;
                    this.confirmPasswordError = '';
                  })
              }
              .padding({ left: 14, right: 14, top: 12, bottom: 12 })
              .width('100%')

              if (this.confirmPasswordError) {
                Text(this.confirmPasswordError)
                  .fontSize(12)
                  .color('#EF4444')
                  .width('100%')
                  .textAlign(TextAlign.Start)
              }
            }
          }

          Button(this.isLoading ? '处理中...' : (this.isLogin ? '登录' : '注册'))
            .width('100%')
            .height(50)
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .backgroundColor('#D4AF37')
            .borderRadius(14)
            .enabled(!this.isLoading)
            .onClick(() => {
              this.handleSubmit();
            })
        }
        .margin({ bottom: 24 })

        Row() {
          Divider()
            .layoutWeight(1)
            .color('rgba(212, 175, 55, 0.2)')

          Text('或')
            .fontSize(13)
            .color('#64748B')
            .margin({ left: 16, right: 16 })

          Divider()
            .layoutWeight(1)
            .color('rgba(212, 175, 55, 0.2)')
        }
        .margin({ bottom: 24 })

        Button('访客模式体验')
          .width('100%')
          .height(50)
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .backgroundColor('transparent')
          .border({
            width: 1,
            color: 'rgba(212, 175, 55, 0.3)'
          })
          .borderRadius(14)
          .fontColor('#D4AF37')
          .enabled(!this.isLoading)
          .onClick(() => {
            this.guestLogin();
          })
      }
      .width('100%')
      .padding(20)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#0B0E14')
  }
}

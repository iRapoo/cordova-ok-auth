# Cordova-ok-auth

2019, @Rapoo

Исправленная библиотека для авторизации и получения доступа к соц. сети Однокласники

**Установка**

    cordova plugin add https://github.com/iRapoo/cordova-ok-auth --variable OK_APP_ID=1234567 //Только цифры

**Пример использования**

    initialize: function() {
            document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function() {
        SocialOk.init('app_id', 'secret', 'key'); //APP ID только цифры
        SocialOk.login(['VALUABLE_ACCESS'], function (value) {
            console.log(value); //value - вернет JSON с token и user (информация аккаунта)
        });
    },

    app.initialize();

**Временное неудобство (исправлено)**

Может возникнуть ошибка:

    * What went wrong:
    Execution failed for task ':app:transformResourcesWithMergeJavaResForDebug'.
    > More than one file was found with OS independent path 'META-INF/DEPENDENCIES'


Для решения данной проблемы необходимо добавить в platform/android/app/build.gradle

    android {
        packagingOptions {
            exclude 'META-INF/NOTICE'
            exclude 'META-INF/LICENSE'
            exclude 'META-INF/DEPENDENCIES'
        }
    }
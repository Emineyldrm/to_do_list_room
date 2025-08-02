FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

 #RUN apt-get update && apt-get install -y openjdk-17-jdk wget unzip git && rm -rf /var/lib/apt/lists/*




# Android SDK'nın komut satırı araçlarını indir
RUN wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip -O sdk-tools.zip && \
    mkdir -p /usr/local/android-sdk/cmdline-tools && \
    unzip sdk-tools.zip -d /usr/local/android-sdk/cmdline-tools && \
    mv /usr/local/android-sdk/cmdline-tools/cmdline-tools /usr/local/android-sdk/cmdline-tools/latest && \
    rm sdk-tools.zip



ENV ANDROID_HOME=/usr/local/android-sdk
ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH


RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "build-tools;33.0.0" "platforms;android-33"

WORKDIR /app
COPY . /app

RUN ./gradlew dependencies

CMD ["./gradlew", "assembleDebug"]

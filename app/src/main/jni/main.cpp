#include "com_yuo_aircraftctrl_MyJNI.h"
#include <android/log.h>
#include <stdio.h>
#include <opencv2/core/utility.hpp>
#include <opencv2/videoio.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/tracking.hpp>
#include <opencv2/ml/ml.hpp>
#include <opencv2/objdetect.hpp>
#include <opencv2/xobjdetect.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/objdetect/detection_based_tracker.hpp>
#include <iostream>
#include <cstring>
#include <opencv2/opencv.hpp>
#include <stdio.h>
#include <string>
#include <ostream>
#include <string>

#define  TAG    "JNI.c"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)


#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)


#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

#define SAMPLE_NUM 100//数据集中的样本数量
#define WIDTH   1320
#define HEIGHT  990
#define TARGET  10

// 定义info信息

static cv::Rect2d roi;
static cv::Ptr<cv::Tracker> tracker;
static cv::String face_cascade_name = "/data/data/com.yuo.aircraftctrl/haarcascade_frontalface_default.xml";
static cv::CascadeClassifier face_cascade;   //定义人脸分类器
static cv::Mat EigenVector,PCA_mean;
static cv::String PCA_filename = "/data/data/com.yuo.aircraftctrl/EigenVector.xml";
static cv::Ptr<cv::ml::SVM> mysvm;



//KCF implement
//输入：帧，识别到的脸的框
//输出：追踪结果
cv::Rect2d KCF_track(cv::Mat frame, cv::Rect2d roi_in){

    // update the tracking result
    tracker->update(frame,roi_in);//初始化在主函数做

    return roi_in;
}

//SVM implement
//输入：待识别的脸
//输出：匹配与否
int SVM_classify(cv::Mat face_out){
    int response = (int)mysvm->predict(face_out);
    return response;
}

//PCA implement
//输入：人脸
//输出：降维结果
cv::Mat PCA_dimension(cv::Mat face, cv::Mat mean, cv::Mat EigenVec){

    cv::Mat face_row = face.reshape(1,1);//展开
    face_row = face_row-mean;
    cv::Mat transpose_vec;
    cv::transpose(EigenVec, transpose_vec);
    cv::Mat point = face_row*transpose_vec; // project into the eigenspace, thus the image becomes a "point"

    return point;
}

//training SVM
void train_SVM(\
               char* graph1, char* graph2,\
               char* graph3, char* graph4,\
               char* graph5, char* graph6,\
               char* graph7, char* graph8,
               char* graph9, int num\
              ){

    //name of samples
    cv::String forename = "/data/data/com.yuo.aircraftctrl/";
    cv::String endname = ".jpg";
    std::ostringstream ostr;

    // set up SVM's parameters
    cv::Ptr<cv::ml::SVM> svm_classifier = cv::ml::SVM::create();
    svm_classifier->setType(cv::ml::SVM::Types::C_SVC);
    svm_classifier->setKernel(cv::ml::SVM::KernelTypes::RBF);
    svm_classifier->setTermCriteria(cv::TermCriteria(cv::TermCriteria::Type::EPS, 1000, 1e-6));

    // train the svm
    //图片数据集
    std::vector<cv::Mat>trainingData;

    cv::Mat trainData;
    //16,4,8,4,2,2,4,4,9
    cv::HOGDescriptor *hog = new cv::HOGDescriptor(\
        cv::Size(16, 4), cv::Size(8, 4), cv::Size(2, 2), cv::Size(4, 4), 9);
    std::vector<float> descriptors;

    int hog_size;
    cv::Mat temp_descriptor;
    std::vector<cv::Mat> HoG_result;
    for(int i=0;i<SAMPLE_NUM;i++){//陌生样本
        ostr<<forename<<(i+1)<<endname;
        trainingData.push_back(cv::imread(ostr.str()));//读数据
        //std::cout<<ostr.str()<<std::endl;
        ostr.str("");

        cv::cvtColor(trainingData.at(i), trainingData.at(i), cv::COLOR_BGR2GRAY);//灰度
        cv::equalizeHist(trainingData.at(i), trainingData.at(i));//直方图均衡
        cv::resize(trainingData.at(i),trainingData.at(i),cv::Size(92,112));
        //cv::waitKey(500);
        hog->compute(trainingData.at(i), descriptors, cv::Size(2, 2), cv::Size(0, 0));//计算hog特征
        if(!i)hog_size=descriptors.size();

        temp_descriptor = cv::Mat(descriptors);
        cv::transpose(temp_descriptor, temp_descriptor);
        HoG_result.push_back(temp_descriptor);
    }

    cv::Mat target_sample;
    for(int i=0;i<num;i++){//目标样本
        switch(i){
            case 0:
                target_sample = cv::imread(graph1);
                break;
            case 1:
                target_sample = cv::imread(graph2);
                break;
            case 2:
                target_sample = cv::imread(graph3);
                break;
            case 3:
                target_sample = cv::imread(graph4);
                break;
            case 4:
                target_sample = cv::imread(graph5);
                break;
            case 5:
                target_sample = cv::imread(graph6);
                break;
            case 6:
                target_sample = cv::imread(graph7);
                break;
            case 7:
                target_sample = cv::imread(graph8);
                break;
            case 8:
                target_sample = cv::imread(graph9);
                break;
            default:break;
        }
        cv::cvtColor(target_sample, target_sample, cv::COLOR_BGR2GRAY);//灰度
        cv::equalizeHist(target_sample, target_sample);//直方图均衡
        cv::resize(target_sample,target_sample,cv::Size(92,112));
        //cv::waitKey(500);
        hog->compute(target_sample, descriptors, cv::Size(2, 2), cv::Size(0, 0));//计算hog特征
        temp_descriptor = cv::Mat(descriptors);
        cv::transpose(temp_descriptor, temp_descriptor);
        HoG_result.push_back(temp_descriptor);
    }

    delete hog;
    hog = NULL;

    cv::Mat dst(static_cast<int>(HoG_result.size()), \
        hog_size, CV_32F);
    for(int i=0;i<SAMPLE_NUM+num;i++)
        HoG_result[i].copyTo(dst.row(i));

    //int max_component = 128;
    cv::PCA pca(dst, cv::noArray(), cv::PCA::DATA_AS_ROW, 0.95);//降到128-d
    trainData = pca.project(dst);

    cv::String PCA_filename = "/data/data/com.yuo.aircraftctrl/EigenVector.xml";
    cv::FileStorage fs(PCA_filename, cv::FileStorage::WRITE);
    write(fs, "EigenVector", pca.eigenvectors);//保存特征向量
    write(fs, "Mean", pca.mean);//保存特征向量
    fs.release();

    //label需要手工标注
    int* labels = new int[SAMPLE_NUM+num];
    for(int i=0;i<SAMPLE_NUM+num;i++){
        labels[i] = i/10;
    }

    cv::Mat labelsMat(SAMPLE_NUM+num, 1, CV_32SC1, labels);
    //std::cout<<labelsMat<<std::endl;
    cv::Mat trainingDataMat;//(SAMPLE_NUM, max_component, CV_32FC1, &trainData);
    trainData.convertTo(trainingDataMat,CV_32FC1);

    cv::Ptr<cv::ml::TrainData> traindata = cv::ml::TrainData::create(trainingDataMat, cv::ml::SampleTypes::ROW_SAMPLE, labelsMat);
    svm_classifier->trainAuto(traindata, 5);

    cv::String SVM_filename = "/data/data/com.yuo.aircraftctrl/SVM_classifier.xml";
    //svm_classifier->train(trainingDataMat, cv::ml::SampleTypes::ROW_SAMPLE, labelsMat);
    svm_classifier->save(SVM_filename);//保存模型

    LOGE("TRAIN_FINISH!!!!!!!!!!!!!!!!");
    delete labels;
    labels = NULL;
}


static std::vector<cv::Rect> temp_rect[2];
static std::vector<int>target_hit;
static int en_track = 0;
static int success = -1;
cv::Rect2d detectAndDisplay(cv::Mat frame, int reiden)
{
    cv::Mat frame_gray;
    static int times = 0;
    /*
    cv::add(frame, cv::Scalar(1.0), frame);  //计算 r+1
    frame.convertTo(frame,CV_32F);
    cv::log(frame, frame);            //计算log(1+r)
    frame.mul(cv::Scalar(50.0));  //计算 r+1
    frame.convertTo(frame,CV_8UC1);
    */
    if(reiden == 1){
        success = -1;
        roi = cv::Rect2d(0.0,0.0,0.0,0.0);
        times = 0;
        en_track = 0;
    }
    cv::equalizeHist(frame, frame_gray);
    if(!en_track){
        std::vector<cv::Rect> faces;
        //cv::cvtColor(frame, frame_gray, COLOR_BGR2GRAY);

        //-- Detect faces

        face_cascade.detectMultiScale(frame_gray, faces, 1.1, 3, 0, cv::Size(70, 70),cv::Size(1000,700));

        //HoG需要的变量
        cv::HOGDescriptor *hog = new cv::HOGDescriptor(\
                cv::Size(16, 4), cv::Size(8, 4), cv::Size(2, 2), cv::Size(4, 4), 9);
        std::vector<float> descriptors;
        cv::Mat HOG_result;

        for (size_t i = 0; i < faces.size(); i++)
        {
            //Point center(faces[i].x + faces[i].width / 2, faces[i].y + faces[i].height / 2);
            //ellipse(frame, center, Size(faces[i].width / 2, faces[i].height / 2), 0, 0, 360, Scalar(255, 0, 255), 4, 8, 0);
            //rectangle(frame, faces[i],Scalar(255,0,0),2,8,0);

            if(!times) {
                temp_rect[0].clear();
                temp_rect[1].clear();
                target_hit.clear();
                temp_rect[0].push_back(cv::Rect(faces[i].x - faces[i].width * 0.5,\
                                       faces[i].y - faces[i].height * 0.5, faces[i].width * 2,\
                                       faces[i].height * 2));
            }
            else{
                temp_rect[1].push_back(faces[i]);
            }


            cv::Mat face = frame_gray(faces[i]);
            cv::resize(face,face,cv::Size(92,112));

            for( int y = 0; y < face.rows; y++ )
            {
                for( int x = 0; x < face.cols; x++ )
                {

                    face.at<uchar>(y,x) = \
                    cv::saturate_cast<uchar>(255.0*pow((double)face.at<uchar>(y,x)/255.0,2.5));
                    //std::cout<<
                }
            }

            //imshow(window_name,face);
            //waitKey(1000);
            hog->compute(face, descriptors, cv::Size(2, 2), cv::Size(0, 0));
            HOG_result = cv::Mat(descriptors);
            cv::transpose(HOG_result, HOG_result);
            cv::Mat face_out = PCA_dimension(HOG_result, PCA_mean, EigenVector);//使用PCA降维
            success = SVM_classify(face_out);//判断是否是目标
            //test
            LOGE("faceid: %,target: %d",i,success);
            std::cout<<success<<std::endl;

            if (success == TARGET) {

                if(!times){
                    target_hit.push_back(1);
                    LOGE("HIT_FIRST");
                    times++;
                }
                else{
                    LOGE("HIT_SECOND");
                    if(i<temp_rect[0].size()){
                        if((temp_rect[0].at(i).x<temp_rect[1].at(i).x)\
                        &&(temp_rect[0].at(i).y<temp_rect[1].at(i).y)\
                        &&((temp_rect[0].at(i).y+temp_rect[0].at(i).height)>(temp_rect[1].at(i).y+temp_rect[1].at(i).height))\
                        &&((temp_rect[0].at(i).x+temp_rect[0].at(i).width)>(temp_rect[1].at(i).x+temp_rect[1].at(i).width)))\
                        {
                            if (target_hit[i] == 1) {
                                en_track=1;
                                roi = faces[i];
                                tracker->init(frame, roi);
                                return roi;
                            } else if (target_hit[i] == 0) {
                                en_track=0;
                            }
                        }
                    }
                }
            } else {
                target_hit.push_back(0);
                times=0;
            }
            //std::cout<<times<<std::endl;

            if(en_track){
                roi = faces[i];
                tracker->init(frame_gray,roi);

                return roi;
            }

        }
        delete hog;
        hog = NULL;
        return roi;
    }
    else{//识别成功，开始追踪
        roi = KCF_track(frame_gray, roi);
        return roi;
    }
    //-- Show what you got
    //imshow(window_name, frame);

}

//识别函数，整合haar,hog,pca,svm,kcf
cv::String Recognition_func(unsigned char pixmap[WIDTH*HEIGHT], int reiden){
    static int init = 1;
    //Load the cascades

    cv::Mat frame = cv::Mat(HEIGHT,WIDTH,CV_8UC1,pixmap);
    cv::resize(frame,frame,cv::Size(640,480));
    //cv::imshow("test",frame);
    cv::String out;
    std::ostringstream ostr;

    if(init){
        if (!face_cascade.load(face_cascade_name))
        {
            LOGE("--(!)Error loading face cascade\n");
            return "Error";
        };
        //PCA需要的变量
        mysvm = cv::ml::SVM::load("/data/data/com.yuo.aircraftctrl/SVM_classifier.xml");
        cv::FileStorage fs(PCA_filename, cv::FileStorage::READ);
        cv::read(fs["EigenVector"], EigenVector);//保存特征向量
        cv::read(fs["Mean"], PCA_mean);//保存特征向量
        tracker = cv::TrackerKCF::create();
        fs.release();

        init = 0;
    }

    //调用检测、识别函数
    cv::Rect2d roi_out = detectAndDisplay(frame, reiden);
    if(roi_out.width!=0){
        ostr<<roi_out.x<<';'<<roi_out.y<<';'<<roi_out.width<<';'<<roi_out.height;
    }
    else{
        ostr<<'0'<<';'<<'0'<<';'<<'0'<<';'<<'0';
    }

    out = ostr.str();
    //std::cout<<ostr.str()<<std::endl;

    return out;
}




/*
 * Class:     com_yuo_aircraftctrl_MyJNI
 * Method:    sayHello
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_yuo_aircraftctrl_MyJNI_sayHello
        (JNIEnv *env, jclass cls){
    return env->NewStringUTF("hello World");
}

/*
 * Class:     com_yuo_aircraftctrl_MyJNI
 * Method:    trainBMP
 * Signature: (ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_yuo_aircraftctrl_MyJNI_trainBMP
        (JNIEnv * env, jclass cls, jint bmpNum, jstring bmp1, jstring bmp2, jstring bmp3, jstring bmp4, jstring bmp5, jstring bmp6, jstring bmp7, jstring bmp8, jstring bmp9){
    const char *bmp1s = env->GetStringUTFChars(bmp1, NULL);
    const char *bmp2s = env->GetStringUTFChars(bmp2, NULL);
    const char *bmp3s = env->GetStringUTFChars(bmp3, NULL);
    const char *bmp4s = env->GetStringUTFChars(bmp4, NULL);
    const char *bmp5s = env->GetStringUTFChars(bmp5, NULL);
    const char *bmp6s = env->GetStringUTFChars(bmp6, NULL);
    const char *bmp7s = env->GetStringUTFChars(bmp7, NULL);
    const char *bmp8s = env->GetStringUTFChars(bmp8, NULL);
    const char *bmp9s = env->GetStringUTFChars(bmp9, NULL);
    LOGI("trainResInfo:\nnum->%d\nb1->%s\nb2->%s\nb3->%s\nb4->%s\nb5->%s\nb6->%s\nb7->%s\nb8->%s\nb9->%s",bmpNum,bmp1s,bmp2s,bmp3s,bmp4s,bmp5s,bmp6s,bmp7s,bmp8s,bmp9s);
    train_SVM((char*)bmp1s,(char*)bmp2s,(char*)bmp3s,(char*)bmp4s,(char*)bmp5s,(char*)bmp6s,(char*)bmp7s,(char*)bmp8s,(char*)bmp9s,(int)bmpNum);
//        FILE* file = NULL;
//    file = fopen("/data/data/com.yuo.aircraftctrl/hello.txt","a");    //创建文件
//    if(file == NULL){        //容错
//        LOGI("文件创建失败%s","222");
//    }
//    fwrite("1111",3,1,file);            //往文件中写文件
//    char buf[200];  /*缓冲区*/
//    FILE *fp;            /*文件指针*/
//    int len;             /*行字符个数*/
//    if((fp = fopen("/storage/emulated/0/deviceid.txt","r")) == NULL)
//    {
//        LOGI("fail to read");
//    }
//    while(fgets(buf,200,fp) != NULL)
//    {
//        len = strlen(buf);
//        buf[len-1] = '\0';  /*去掉换行符*/
//        LOGI("%s %d \n",buf,len - 1);
//    }
//    fclose(file);
    return env->NewStringUTF("hello World");
}

/*
 * Class:     com_yuo_aircraftctrl_MyJNI
 * Method:    Identify
 * Signature: (Landroid/graphics/Bitmap;)com/yuo/aircraftctrl/IdentiResult;
 */
JNIEXPORT jstring JNICALL Java_com_yuo_aircraftctrl_MyJNI_Identify
        (JNIEnv * env, jclass jls, jbyteArray buffer, jint w, jint h){
    unsigned char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(buffer, 0);
    int chars_len = env->GetArrayLength(buffer);
    chars = new unsigned char[chars_len + 1];
    memset(chars,0,chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;
    env->ReleaseByteArrayElements(buffer, bytes, 0);

    const  char* for_return = Recognition_func(chars, (int)w).c_str();
    delete chars;
    chars = NULL;
    return env->NewStringUTF(for_return);
}


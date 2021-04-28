import axios from 'axios'

export function request(config){
  //1.创建axios实例
  const instance = axios.create({
    //2.创建实例时传入axios的配置
    baseURL: "http://127.0.0.1:8080/apis",
    timeout: 5000
  })
  //3.发送网络请求, instance返回的是一个promise
  return instance(config);
}

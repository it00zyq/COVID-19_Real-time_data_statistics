<template>
  <div class="hello">
    <div class="container">
        <div class="search">
          <el-input style="display: inline-block; width: 250px; position: absolute" v-model="country" size="mini" placeholder="请输入国家名称"></el-input>
          <el-button style="display: inline-block; position: absolute; right: 0px" type="primary" size="mini" @click="getDataList()">搜索</el-button>
        </div>
      <template>
        <template>
          <el-table
            fit
            border
            :data="dataList"
            highlight-current-row >
            <el-table-column
              prop="id"
              label="序号"
              width="120">
            </el-table-column>
            <el-table-column
              prop="country"
              label="国家/地区"
              width="250">
            </el-table-column>
            <el-table-column
              prop="province"
              label="省份/州"
              width="250">
            </el-table-column>
            <el-table-column
              prop="total"
              label="累计确诊病例"
              width="180">
            </el-table-column>
            <el-table-column
              prop="newCount"
              label="新增确诊病例"
              width="180">
            </el-table-column>
            <el-table-column
              label="修改"
              width="200">
              <template slot-scope="detail">
                <el-button type="success" size="mini" @click="detailData(detail.row.id)">查看详情</el-button>
                <template>
                  <el-dialog :visible="showDialog" :before-close="closeDialog">
                    <el-table
                      fit
                      border
                      :data="details">
                      <el-table-column
                        prop="date"
                        label="统计日期"
                        width="380">
                      </el-table-column>
                      <el-table-column
                        prop="count"
                        label="累计确诊人数"
                        width="200">
                      </el-table-column>
                    </el-table>
                  </el-dialog>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20, 40]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="itemTotal">
          </el-pagination>
        </template>
      </template>
    </div>
  </div>
</template>

<script>
  import InfiniteScroll from 'vue-infinite-scroll'
  import {request} from "../../network/request";
  export default {
    name: "Index",
    data() {
      return {
        country: "",
        pageNum: 1,
        pageSize: 10,
        dataList: [],
        itemTotal: 0,
        pageTotal: 0,
        showDialog: false,
        details: []
      }
    },
    methods: {
      getDataList(){
        request({
          url: "/data/",
          method: "get",
          params: {
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            country: this.country
          }
        }).then(res => {
          console.log(res)
          console.log(res.data.data.data)
          if (parseInt(res.data.code) === 200){
            this.dataList = res.data.data.data.items
            this.pageNum = res.data.data.data.pageNum
            this.pageSize = res.data.data.data.pageSize
            this.pageTotal = res.data.data.data.pageTotal
            this.itemTotal = res.data.data.data.itemTotal
          }else {
            this.$message({type: 'error', center: true, message: res.data.message})
          }
        }).catch(res => {
          this.$message({type: 'error', center: true, message: "网络繁忙，请稍后重试"})
        })
      },
      handleSizeChange(size) {
        this.pageSize = size
        this.getDataList()
      },
      handleCurrentChange(num) {
        this.pageNum = num
        this.getDataList()
      },
      closeDialog() {
        this.showDialog = false
      },
      detailData(id) {
        request({
          url: "/detail/",
          method: "get",
          params: {
            id: id
          }
        }).then(res => {
          if (parseInt(res.data.code) === 200){
            this.showDialog = true
            this.details = res.data.data.data
          }else {
            this.$message({type: 'error', center: true, message: res.data.message})
          }
        }).catch(res => {
          this.$message({type: 'error', center: true, message: "网络繁忙，请稍后重试"})
        })
      }
    },
    activated() {
      this.getDataList()
    },
    computed: {},
    components: {
      InfiniteScroll
    }
  }
</script>

<style scoped>
  .search {
    height: 40px;
    width: 660px;
    display: block;
    position: relative;
  }
  .hello {
    text-align: center;
  }
</style>

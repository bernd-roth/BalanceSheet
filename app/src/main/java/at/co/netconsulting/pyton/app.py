from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "postgresql://postgres:password@hostnameOrip:5432/incomeexpense"
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
migrate = Migrate(app, db)

class IncomeExpenseModel(db.Model):
        __tablename__ = 'incomeexpense'

        id = db.Column(db.Integer, primary_key=True)
        orderdate = db.Column(db.String())
        who = db.Column(db.String())
        location = db.Column(db.Integer())
        income = db.Column(db.Numeric(16,2))
        expense = db.Column(db.Numeric(16,2))

        def __init__(self, orderdate, who, location, income, expense):
                self.orderdate = orderdate
                self.who = who
                self.location = location
                self.income = income
                self.expense = expense

        def __repr__(self):
                return f"IncomeExpenseModel('{self.id}', '{self.orderdate}', '{self.who}', '{self.location}', '{self.income}', '{self.expense}')"

@app.route('/')
def hello():
        return {"hello": "world"}

@app.route('/incomeexpense/<id>', methods=['GET'])
def handle_incomexpense(id):
        incomeexpense = IncomeExpenseModel.query.get_or_404(id)

        if request.method == 'GET':
                response = {
                        "orderdate": incomeexpense.orderdate,
                        "who": incomeexpense.who,
                        "location": incomeexpense.location
                }
                return {"message": "success", "incomeexpense": response}
                response = {
                        "orderdate": incomeexpense.orderdate,
                        "who": incomeexpense.who,
                        "location": incomeexpense.location,
                        "income": incomeexpense.income,
                        "expense": incomeexpense.expense
                }

@app.route('/incomeexpense/add', methods=['POST'])
def handle_incomexpense_add():
        if request.method == 'POST' and 'orderdate' in request.form and 'who' in request.form and 'location' in request.form and 'income' in request.form and 'expense' in request.form:
                orderdate = request.form.get('orderdate')
                who = request.form.get('who')
                location = request.form.get('location')
                income = request.form.get('income')
                expense = request.form.get('expense')
                entry = IncomeExpenseModel(o, w, l, i, e)
                db.session.add(entry)
                db.session.commit()
                return jsonify({"message":"Successfully inserted!"})

if __name__ == '__main__':
        app.run(host="0.0.0.0") #debug=True)
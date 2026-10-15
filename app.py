from pathlib import Path

from flask import Flask, jsonify, render_template, request

from service import FakeDanishPersonService


ROOT = Path(__file__).resolve().parent
service = FakeDanishPersonService(
    names_path=str(ROOT / "person-names.json"),
    addresses_path=str(ROOT / "addresses.sql"),
)


def create_app() -> Flask:
    app = Flask(__name__)

    @app.get("/")
    def index():
        return render_template("index.html")

    @app.get("/api/cpr")
    def cpr():
        return jsonify(service.fake_cpr())

    @app.get("/api/name-gender")
    def name_gender():
        return jsonify(service.fake_name_gender())

    @app.get("/api/name-gender-dob")
    def name_gender_dob():
        return jsonify(service.fake_name_gender_dob())

    @app.get("/api/cpr-name-gender")
    def cpr_name_gender():
        return jsonify(service.fake_cpr_name_gender())

    @app.get("/api/cpr-name-gender-dob")
    def cpr_name_gender_dob():
        return jsonify(service.fake_cpr_name_gender_dob())

    @app.get("/api/address")
    def address():
        return jsonify(service.fake_address())

    @app.get("/api/mobile")
    def mobile():
        return jsonify(service.fake_mobile_phone_number())

    @app.get("/api/person")
    def person():
        return jsonify(service.fake_person())

    @app.get("/api/persons")
    def persons_bulk():
        count = int(request.args.get("count", "2"))
        try:
            return jsonify(service.fake_person_bulk(count))
        except ValueError:
            return jsonify({"error": "count must be between 2 and 100"}), 400

    return app


app = create_app()


if __name__ == "__main__":
    app.run()
